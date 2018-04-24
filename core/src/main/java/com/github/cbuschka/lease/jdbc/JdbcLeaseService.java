package com.github.cbuschka.lease.jdbc;

import com.github.cbuschka.lease.GrantedLease;
import com.github.cbuschka.lease.LeaseCookieGenerator;
import com.github.cbuschka.lease.LeaseHolderInfoProvider;
import com.github.cbuschka.lease.LeaseUnavailableException;
import com.github.cbuschka.lease.LeaseValidityRegistry;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

public class JdbcLeaseService implements com.github.cbuschka.lease.LeaseService
{
	private JdbcLeaseDao jdbcLeaseDao;
	private LeaseHolderInfoProvider leaseHolderInfoProvider;
	private LeaseCookieGenerator leaseCookieGenerator;
	private LeaseValidityRegistry leaseValidityRegistry;
	private PlatformTransactionManager platformTransactionManager;

	public void setJdbcLeaseDao(JdbcLeaseDao jdbcLeaseDao)
	{
		this.jdbcLeaseDao = jdbcLeaseDao;
	}

	public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager)
	{
		this.platformTransactionManager = platformTransactionManager;
	}

	public void setLeaseCookieGenerator(LeaseCookieGenerator leaseCookieGenerator)
	{
		this.leaseCookieGenerator = leaseCookieGenerator;
	}

	public void setLeaseHolderInfoProvider(LeaseHolderInfoProvider leaseHolderInfoProvider)
	{
		this.leaseHolderInfoProvider = leaseHolderInfoProvider;
	}

	public void setLeaseValidityRegistry(LeaseValidityRegistry leaseValidityRegistry)
	{
		this.leaseValidityRegistry = leaseValidityRegistry;
	}

	public GrantedLease acquire(String lease) throws LeaseUnavailableException
	{
		return getNewTxTransactionTemplate().execute(new TransactionCallback<GrantedLease>()
		{
			@Override
			public GrantedLease doInTransaction(TransactionStatus status)
			{
				Duration validity = leaseValidityRegistry.getValidityFor(lease);

				return acquire(lease, validity);
			}
		});
	}

	public GrantedLease acquire(String leaseName, Duration validity) throws LeaseUnavailableException
	{
		return getNewTxTransactionTemplate().execute(new TransactionCallback<GrantedLease>()
		{
			@Override
			public GrantedLease doInTransaction(TransactionStatus status)
			{
				String holder = leaseHolderInfoProvider.getHolderInfo();

				GrantedLease grantedLease = acquireOrRenew(leaseName, null, holder, validity);
				if (leaseName == null)
				{
					throw new LeaseUnavailableException(leaseName);
				}

				return grantedLease;

			}
		});
	}

	public boolean isValid(GrantedLease lease)
	{
		return getNewTxTransactionTemplate().execute(new TransactionCallback<Boolean>()
		{
			@Override
			public Boolean doInTransaction(TransactionStatus status)
			{
				Optional<LeaseDto> optionalLease = jdbcLeaseDao.selectLease(lease.getName());
				if (!optionalLease.isPresent())
				{
					return false;
				}

				LeaseDto leaseDto = optionalLease.get();
				long validityLeftMillis = leaseDto.getExpiresAt().getTime() - leaseDto.getNow().getTime();
				return validityLeftMillis > 0 && leaseDto.getCookie().equals(lease.getCookie());
			}
		});
	}

	public GrantedLease renew(GrantedLease lease) throws LeaseUnavailableException
	{
		return getNewTxTransactionTemplate().execute(new TransactionCallback<GrantedLease>()
		{
			@Override
			public GrantedLease doInTransaction(TransactionStatus status)
			{
				Duration validity = leaseValidityRegistry.getValidityFor(lease.getName());
				return renew(lease, validity);
			}
		});
	}

	public GrantedLease renew(GrantedLease lease, Duration validity) throws LeaseUnavailableException
	{
		return getNewTxTransactionTemplate().execute(new TransactionCallback<GrantedLease>()
		{
			@Override
			public GrantedLease doInTransaction(TransactionStatus status)
			{
				String holderName = leaseHolderInfoProvider.getHolderInfo();

				GrantedLease renewedLease = acquireOrRenew(lease.getName(), lease.getCookie(), holderName, validity);
				if (renewedLease == null)
				{
					throw new LeaseUnavailableException(lease.getName());
				}

				return renewedLease;
			}
		});
	}

	public void release(GrantedLease lease)
	{
		getNewTxTransactionTemplate().execute(new TransactionCallback<Void>()
		{
			@Override
			public Void doInTransaction(TransactionStatus status)
			{
				jdbcLeaseDao.deleteLease(lease.getName(), lease.getCookie());
				return null;
			}
		});
	}

	private GrantedLease acquireOrRenew(String leaseName, String expectedCookie, String holder, Duration validity)
	{
		Optional<LeaseDto> optionalLease = jdbcLeaseDao.selectLeaseForUpdate(leaseName);
		if (optionalLease.isPresent())
		{
			LeaseDto leaseDto = optionalLease.get();
			long validityLeftMillis = leaseDto.getExpiresAt().getTime() - leaseDto.getNow().getTime();
			if (validityLeftMillis > 0 && !leaseDto.getCookie().equals(expectedCookie))
			{
				return null;
			}
			else
			{
				String cookie = this.leaseCookieGenerator.generateCookie();
				jdbcLeaseDao.updateLease(leaseName, cookie, holder, validity);
				return new GrantedLease(leaseName, cookie, new Date(System.currentTimeMillis() + validity.toMillis()));
			}
		}
		else
		{
			String cookie = this.leaseCookieGenerator.generateCookie();
			jdbcLeaseDao.insertLease(leaseName, cookie, holder, validity);
			return new GrantedLease(leaseName, cookie, new Date(System.currentTimeMillis() + validity.toMillis()));
		}
	}

	private TransactionTemplate getNewTxTransactionTemplate()
	{
		TransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		return new TransactionTemplate(this.platformTransactionManager, txDef);
	}
}
