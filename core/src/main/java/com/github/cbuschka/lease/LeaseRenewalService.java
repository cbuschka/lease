package com.github.cbuschka.lease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;

@Service
public class LeaseRenewalService
{
	private static Logger logger = LoggerFactory.getLogger(LeaseRenewalService.class);

	private final Timer timer = new Timer("LeaseTimer", true);

	private LeaseService leaseService;
	private LeaseStatusService leaseStatusService;
	private LeaseManagerConfiguration leaseManagerConfiguration;

	public void renewLeases()
	{
		logger.trace("Renewing leases...");

		for (String leaseName : leaseStatusService.getKnownLeases())
		{
			try
			{
				GrantedLease grantedLease = leaseStatusService.getLeaseOrNull(leaseName);
				if (grantedLease == null)
				{
					grantedLease = leaseService.acquire(leaseName);

					logger.trace("Lease {} acquired.", leaseName);

					leaseStatusService.updateLease(grantedLease);
				}
				else
				{
					GrantedLease renewedLease = leaseService.renew(grantedLease);

					logger.trace("Lease {} renewed.", leaseName);

					leaseStatusService.updateLease(renewedLease);
				}
			}
			catch (LeaseUnavailableException ex)
			{
				logger.debug("Lease {} not available.", leaseName);

				leaseStatusService.removeLease(leaseName);
			}
		}
	}

	void init()
	{
		logger.trace("Starting...");

		this.timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					renewLeases();
				}
				catch (Exception ex)
				{
					logger.error("Error while renewing releases.", ex);
				}
			}
		}, this.leaseManagerConfiguration.getIntialRenewalDelay().toMillis(), this.leaseManagerConfiguration.getRenewalDelay().toMillis());

		logger.trace("Started.");
	}

	void destroy()
	{
		logger.trace("Destroying...");

		this.timer.cancel();

		logger.trace("Destroyed.");
	}

	public void setLeaseManagerConfiguration(LeaseManagerConfiguration leaseManagerConfiguration)
	{
		this.leaseManagerConfiguration = leaseManagerConfiguration;
	}

	public void setLeaseService(LeaseService leaseService)
	{
		this.leaseService = leaseService;
	}

	public void setLeaseStatusService(LeaseStatusService leaseStatusService)
	{
		this.leaseStatusService = leaseStatusService;
	}
}
