package com.github.cbuschka.lease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;

public class LeaseManagerServiceImpl implements LeaseManagerService
{
	private static Logger log = LoggerFactory.getLogger(LeaseManagerServiceImpl.class);

	private LeaseManagerConfiguration leaseManagerConfiguration;
	private LeaseService leaseService;
	private LeaseStatusService leaseStatusService;
	private LeaseRenewalService leaseRenewalService;

	@PostConstruct
	protected void init()
	{
		log.debug("Starting up...");

		this.leaseRenewalService.init();
	}

	@PreDestroy
	protected void destroy()
	{
		log.debug("Destroying...");

		this.leaseRenewalService.destroy();
	}

	@Override
	public boolean isEnabled()
	{
		return this.leaseManagerConfiguration.isEnabled();
	}

	public void setLeaseManagerConfiguration(LeaseManagerConfiguration leaseManagerConfiguration)
	{
		this.leaseManagerConfiguration = leaseManagerConfiguration;
	}

	public void setLeaseRenewalService(LeaseRenewalService leaseRenewalService)
	{
		this.leaseRenewalService = leaseRenewalService;
	}

	public void setLeaseService(LeaseService leaseService)
	{
		this.leaseService = leaseService;
	}

	public void setLeaseStatusService(LeaseStatusService leaseStatusService)
	{
		this.leaseStatusService = leaseStatusService;
	}

	// @Override
	public void acquire(String lease) throws LeaseUnavailableException
	{
		this.leaseService.acquire(lease);
	}

	// @Override
	public void acquire(String lease, Duration validity) throws LeaseUnavailableException
	{
		this.leaseService.acquire(lease, validity);
	}

	// @Override
	public void release(String lease)
	{
		this.leaseStatusService.release(lease);
	}

	@Override
	public boolean isAcquired(String lease)
	{
		return this.leaseStatusService.isCurrentlyHolding(lease);
	}
}
