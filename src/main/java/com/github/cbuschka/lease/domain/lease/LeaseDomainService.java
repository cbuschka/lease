package com.github.cbuschka.lease.domain.lease;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
public class LeaseDomainService
{
	private static final Random random = new Random();

	@Autowired
	private LeaseDao leaseDao;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public GrantedLease acquire(LeaseType leaseType) throws LeaseUnavailableException
	{
		String holder = getHolder();

		GrantedLease lease = acquireOrRenew(leaseType, null, holder, leaseType.getValidityDurationMillis());
		if (lease == null)
		{
			throw new LeaseUnavailableException();
		}

		return lease;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean isValid(GrantedLease lease)
	{
		Optional<LeaseDto> optionalLease = this.leaseDao.selectLease(lease.getName().name());
		if (!optionalLease.isPresent())
		{
			return false;
		}

		LeaseDto leaseDto = optionalLease.get();
		long validityLeftMillis = leaseDto.getExpiryAt().getTime() - leaseDto.getNow().getTime();
		return validityLeftMillis > 0 && leaseDto.getCookie().equals(lease.getCookie());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public GrantedLease renew(GrantedLease lease, long validityDurationMillis) throws LeaseUnavailableException
	{
		String holder = getHolder();

		GrantedLease renewedLease = acquireOrRenew(lease.getName(), lease.getCookie(), holder, validityDurationMillis);
		if (renewedLease == null)
		{
			throw new LeaseUnavailableException();
		}

		return renewedLease;
	}

	private GrantedLease acquireOrRenew(LeaseType leaseType, String expectedCookie, String holder, long validityDurationMillis)
	{
		Optional<LeaseDto> optionalLease = leaseDao.selectLeaseForUpdate(leaseType.name());
		if (optionalLease.isPresent())
		{
			LeaseDto leaseDto = optionalLease.get();
			long validityLeftMillis = leaseDto.getExpiryAt().getTime() - leaseDto.getNow().getTime();
			if (validityLeftMillis > 0 && !leaseDto.getCookie().equals(expectedCookie))
			{
				return null;
			}
			else
			{
				String cookie = generateCookie();
				leaseDao.updateLease(leaseType.name(), cookie, holder, validityDurationMillis);
				return new GrantedLease(leaseType, cookie, new Date(System.currentTimeMillis() + validityDurationMillis));
			}
		}
		else
		{
			String cookie = generateCookie();
			leaseDao.insertLease(leaseType.name(), cookie, holder, validityDurationMillis);
			return new GrantedLease(leaseType, cookie, new Date(System.currentTimeMillis() + validityDurationMillis));
		}
	}

	private String getHolder()
	{
		StringBuilder buf = new StringBuilder();
		buf.append(System.getProperty("user.name", "no-user.name")).append("@");

		try
		{
			buf.append(InetAddress.getLocalHost().getHostName());
		}
		catch (UnknownHostException ex)
		{
			buf.append("localhost");
		}

		try
		{
			Path selfPath = new File("/proc/self").toPath().toRealPath();
			buf.append(" (" + selfPath.toFile().getName() + ")");
		}
		catch (IOException ex)
		{
		}

		return buf.toString();
	}


	private String generateCookie()
	{
		byte[] bytes = new byte[16];
		random.nextBytes(bytes);
		return new BigInteger(bytes).abs().toString(16).toLowerCase();
	}
}
