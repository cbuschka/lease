package com.github.cbuschka.lease.config;

import com.github.cbuschka.lease.DefaultLeaseCookieGenerator;
import com.github.cbuschka.lease.DefaultLeaseHolderInfoProvider;
import com.github.cbuschka.lease.DefaultLeaseValidityRegistry;
import com.github.cbuschka.lease.LeaseManagerConfiguration;
import com.github.cbuschka.lease.LeaseManagerService;
import com.github.cbuschka.lease.LeaseManagerServiceImpl;
import com.github.cbuschka.lease.LeaseRenewalService;
import com.github.cbuschka.lease.LeaseService;
import com.github.cbuschka.lease.LeaseStatusService;
import com.github.cbuschka.lease.event.LeaseEventBroadcaster;
import com.github.cbuschka.lease.jdbc.DefaultJdbcLeaseDao;
import com.github.cbuschka.lease.jdbc.JdbcLeaseDao;
import com.github.cbuschka.lease.jdbc.JdbcLeaseService;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

public class LeaseManagerServiceBuilder
{
	private LeaseManagerConfiguration leaseManagerConfiguration;
	private LeaseServiceBuilder leaseServiceBuilder;

	public LeaseManagerServiceBuilder()
	{
	}

	public LeaseManagerServiceBuilder setLeaseManagerConfiguration(LeaseManagerConfiguration leaseManagerConfiguration)
	{
		this.leaseManagerConfiguration = leaseManagerConfiguration;
		return this;
	}

	public LeaseManagerService build()
	{
		LeaseManagerConfiguration leaseManagerConfiguration = getOrCreateLeaseManagerConfig();

		LeaseService leaseService = buildLeaseService();

		LeaseStatusService leaseStatusService = getOrCreateLeaseStatusService(leaseManagerConfiguration, leaseService);

		LeaseRenewalService leaseRenewalService = getOrCreateLeaseRenewalService(leaseStatusService, leaseService, leaseManagerConfiguration);

		LeaseManagerServiceImpl leaseManagerService = buildLeaseManagerService(leaseService, leaseStatusService, leaseManagerConfiguration, leaseRenewalService);
		return leaseManagerService;
	}

	private LeaseService buildLeaseService()
	{
		if (this.leaseServiceBuilder == null)
		{
			throw new IllegalStateException("No LeaseManagerService configured.");
		}
		return this.leaseServiceBuilder.build();
	}

	private LeaseRenewalService getOrCreateLeaseRenewalService(LeaseStatusService leaseStatusService, com.github.cbuschka.lease.LeaseService leaseService, LeaseManagerConfiguration leaseManagerConfiguration)
	{
		LeaseRenewalService leaseRenewalService = new LeaseRenewalService();
		leaseRenewalService.setLeaseStatusService(leaseStatusService);
		leaseRenewalService.setLeaseService(leaseService);
		leaseRenewalService.setLeaseManagerConfiguration(leaseManagerConfiguration);
		return leaseRenewalService;
	}

	private LeaseManagerServiceImpl buildLeaseManagerService(LeaseService leaseService, LeaseStatusService leaseStatusService, LeaseManagerConfiguration leaseManagerConfiguration, LeaseRenewalService leaseRenewalservice)
	{
		LeaseManagerServiceImpl leaseManagerService = new LeaseManagerServiceImpl();
		leaseManagerService.setLeaseService(leaseService);
		leaseManagerService.setLeaseStatusService(leaseStatusService);
		leaseManagerService.setLeaseManagerConfiguration(leaseManagerConfiguration);
		leaseManagerService.setLeaseRenewalService(leaseRenewalservice);
		return leaseManagerService;
	}

	private LeaseStatusService getOrCreateLeaseStatusService(LeaseManagerConfiguration leaseManagerConfiguration, LeaseService leaseService)
	{
		LeaseStatusService leaseStatusService = new LeaseStatusService();
		leaseStatusService.setLeaseService(leaseService);
		leaseStatusService.setLeaseEventBroadcaster(getOrCreateLeaseEventBroadcaster(leaseManagerConfiguration));
		leaseStatusService.setLeaseManagerConfiguration(leaseManagerConfiguration);
		return leaseStatusService;
	}

	private LeaseManagerConfiguration getOrCreateLeaseManagerConfig()
	{
		if (this.leaseManagerConfiguration != null)
		{
			return leaseManagerConfiguration;
		}
		else
		{
			LeaseManagerConfiguration leaseManagerConfiguration = new LeaseManagerConfiguration();
			leaseManagerConfiguration.setEnabled(true);
			return leaseManagerConfiguration;
		}
	}

	private LeaseEventBroadcaster getOrCreateLeaseEventBroadcaster(LeaseManagerConfiguration leaseManagerConfiguration)
	{
		LeaseEventBroadcaster broadcaster = new LeaseEventBroadcaster();
		broadcaster.setLeaseEventListeners(leaseManagerConfiguration.getLeaseEventListeners());
		return broadcaster;
	}

	public LeaseManagerServiceBuilder withLeaseService(LeaseService leaseService)
	{
		this.leaseServiceBuilder = new DefinedLeaseServiceBuilder(leaseService);
		return this;
	}

	public JdbcLeaseServiceBuilder jdbc()
	{
		this.leaseServiceBuilder = new JdbcLeaseServiceBuilder();

		return (JdbcLeaseServiceBuilder) this.leaseServiceBuilder;
	}

	public static abstract class LeaseServiceBuilder
	{
		protected abstract LeaseService build();
	}

	public static class JdbcLeaseServiceBuilder extends LeaseServiceBuilder
	{
		private PlatformTransactionManager platformTransactionManager;
		private DataSource dataSource;

		private JdbcLeaseServiceBuilder()
		{
		}

		public JdbcLeaseServiceBuilder setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager)
		{
			this.platformTransactionManager = platformTransactionManager;
			return this;
		}

		public JdbcLeaseServiceBuilder setDataSource(DataSource dataSource)
		{
			this.dataSource = dataSource;
			return this;
		}

		@Override
		protected LeaseService build()
		{
			JdbcLeaseService jdbcLeaseService = new JdbcLeaseService();
			jdbcLeaseService.setJdbcLeaseDao(getOrCreateJdbcLeaseDao());
			jdbcLeaseService.setLeaseCookieGenerator(new DefaultLeaseCookieGenerator());
			jdbcLeaseService.setLeaseHolderInfoProvider(new DefaultLeaseHolderInfoProvider());
			jdbcLeaseService.setLeaseValidityRegistry(new DefaultLeaseValidityRegistry());
			jdbcLeaseService.setPlatformTransactionManager(this.platformTransactionManager);
			return jdbcLeaseService;
		}

		private JdbcLeaseDao getOrCreateJdbcLeaseDao()
		{
			DefaultJdbcLeaseDao defaultJdbcLeaseDao = new DefaultJdbcLeaseDao();
			defaultJdbcLeaseDao.setDataSource(this.dataSource);
			return defaultJdbcLeaseDao;
		}
	}

	private static class DefinedLeaseServiceBuilder extends LeaseServiceBuilder
	{
		private LeaseService leaseService;

		private DefinedLeaseServiceBuilder(LeaseService leaseService)
		{
			this.leaseService = leaseService;
		}

		@Override
		protected LeaseService build()
		{
			return this.leaseService;
		}
	}
}
