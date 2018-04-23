package com.github.cbuschka.lease.domain.lease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LeaseStatusService
{
	private static Logger logger = LoggerFactory.getLogger(LeaseStatusService.class);

	private Map<LeaseType, GrantedLease> leases = new ConcurrentHashMap<>();

	@Value("${LeaseStatusService.enabled:true}")
	private boolean enabled;

	@Autowired
	private LeaseDomainService leaseDomainService;

    public boolean isEnabled() {
        return enabled;
    }

    @PostConstruct
    private void init() {
	    if( !this.enabled) {
            logger.warn("Leases NOT enabled! Use in dev only!");
        }
    }

	public boolean isCurrentlyHolding(LeaseType leaseType)
	{
		if( !enabled ) {
			return true;
		}

		GrantedLease grantedLease = leases.get(leaseType);
		if (grantedLease == null)
		{
			return false;
		}

		boolean valid = grantedLease.getValidUntil().getTime() > System.currentTimeMillis();
		if (!valid)
		{
			return false;
		}

		return this.leaseDomainService.isValid(grantedLease);
	}

	void updateLease(LeaseType leaseType, GrantedLease lease)
	{
		if (!this.leases.containsKey(leaseType))
		{
			onLeaseAcquired(lease);
		}
		else
		{
			onLeaseRenewed(lease);
		}

		this.leases.put(leaseType, lease);
	}

	private void onLeaseRenewed(GrantedLease lease)
	{
		logger.debug("Renewed lease {}, valid for {} millis.", lease.getName(), lease.getValidUntil().getTime() - System.currentTimeMillis());
	}

	private void onLeaseAcquired(GrantedLease lease)
	{
		logger.info("Acquired lease {}, valid for {} millis.", lease.getName(), lease.getValidUntil().getTime() - System.currentTimeMillis());
	}

	GrantedLease getLeaseOrNull(LeaseType leaseType)
	{
		return this.leases.get(leaseType);
	}

	void removeLease(LeaseType leaseType)
	{
		boolean wasHolder = this.leases.containsKey(leaseType);
		this.leases.remove(leaseType);
		if (wasHolder)
		{
			onLeaseLost(leaseType);
		}
	}

	private void onLeaseLost(LeaseType leaseType)
	{
		logger.info("Lost lease {}.", leaseType.name());
	}
}
