package com.github.cbuschka.lease.domain.lease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LeaseRenewalService
{
	private static Logger logger = LoggerFactory.getLogger(LeaseRenewalService.class);

	@Autowired
	private LeaseDomainService leaseDomainService;
	@Autowired
	private LeaseStatusService leaseStatusService;

	@Scheduled(initialDelayString = "${LeaseRenewalService.initialDelayMillis}", fixedDelayString = "${LeaseRenewalService.delayMillis}")
	public void renewLeases()
	{
		if( !this.leaseStatusService.isEnabled() ) {
			return;
		}

		logger.trace("Renewing leases...");

		for (LeaseType leaseType : LeaseType.values())
		{
			try
			{
				GrantedLease grantedLease = leaseStatusService.getLeaseOrNull(leaseType);
				if (grantedLease == null)
				{
					GrantedLease renewedLease = leaseDomainService.acquire(leaseType);

					logger.trace("Lease {} acquired.", leaseType.name());

					leaseStatusService.updateLease(leaseType, renewedLease);
				}
				else
				{
					GrantedLease renewedLease = leaseDomainService.renew(grantedLease, leaseType.getValidityDurationMillis());

					logger.trace("Lease {} renewed.", leaseType.name());

					leaseStatusService.updateLease(leaseType, renewedLease);
				}
			}
			catch (LeaseUnavailableException ex)
			{
				logger.debug("Lease {} not available.", leaseType.name());

				leaseStatusService.removeLease(leaseType);
			}
		}
	}
}
