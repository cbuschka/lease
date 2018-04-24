package com.github.cbuschka.lease;

import com.github.cbuschka.lease.event.LeaseEvent;
import com.github.cbuschka.lease.event.LeaseEventBroadcaster;
import com.github.cbuschka.lease.event.LeaseEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeaseStatusService
{
	private static Logger logger = LoggerFactory.getLogger(LeaseStatusService.class);

	private Map<String, GrantedLease> leases = new ConcurrentHashMap<>();

	private LeaseManagerConfiguration leaseManagerConfiguration;
	private LeaseEventBroadcaster leaseEventBroadcaster;
	private LeaseService leaseService;

	public void init()
	{
		addAutoAcquiredReleases();
	}

	public void setLeaseManagerConfiguration(LeaseManagerConfiguration leaseManagerConfiguration)
	{
		this.leaseManagerConfiguration = leaseManagerConfiguration;
	}

	public void setLeaseEventBroadcaster(LeaseEventBroadcaster leaseEventBroadcaster)
	{
		this.leaseEventBroadcaster = leaseEventBroadcaster;
	}

	public void setLeaseService(LeaseService leaseService)
	{
		this.leaseService = leaseService;
	}

	private void addAutoAcquiredReleases()
	{
		for (String lease : leaseManagerConfiguration.getAutoAcquiredLeases())
		{
			this.leases.put(lease, null);
		}
	}

	boolean isEnabled()
	{
		return this.leaseManagerConfiguration.isEnabled();
	}

	private void checkEnabled()
	{
		if (!isEnabled())
		{
			throw new IllegalStateException("Leases not enabled.");
		}
	}

	boolean isCurrentlyHolding(String leaseName)
	{
		checkEnabled();

		GrantedLease grantedLease = leases.get(leaseName);
		if (grantedLease == null)
		{
			return false;
		}

		return isValid(grantedLease);
	}

	private boolean isValid(GrantedLease grantedLease)
	{
		boolean valid = grantedLease.getValidUntil().getTime() > System.currentTimeMillis();
		if (!valid)
		{
			return false;
		}

		return this.leaseService.isValid(grantedLease);
	}

	void updateLease(GrantedLease lease)
	{
		String leaseName = lease.getName();
		if (!this.leases.containsKey(leaseName))
		{
			onLeaseAcquired(lease);
		}
		else
		{
			onLeaseRenewed(lease);
		}

		this.leases.put(leaseName, lease);
	}

	private void onLeaseRenewed(GrantedLease lease)
	{
		logger.debug("Renewed lease {}, valid for {} millis.", lease.getName(), lease.getValidUntil().getTime() - System.currentTimeMillis());

		this.leaseEventBroadcaster.broadcast(new LeaseEvent(LeaseEventType.RENEWED, lease.getName()));
	}

	private void onLeaseAcquired(GrantedLease lease)
	{
		logger.info("Acquired lease {}, valid for {} millis.", lease.getName(), lease.getValidUntil().getTime() - System.currentTimeMillis());

		this.leaseEventBroadcaster.broadcast(new LeaseEvent(LeaseEventType.ACQUIRED, lease.getName()));
	}

	GrantedLease getLeaseOrNull(String leaseName)
	{
		return this.leases.get(leaseName);
	}

	void removeLease(String leaseName)
	{
		boolean wasHolder = this.leases.containsKey(leaseName);
		this.leases.remove(leaseName);
		if (wasHolder)
		{
			onLeaseLost(leaseName);
		}
	}

	private void onLeaseLost(String leaseName)
	{
		logger.info("Lost lease {}.", leaseName);

		this.leaseEventBroadcaster.broadcast(new LeaseEvent(LeaseEventType.RELEASED, leaseName));
	}

	Iterable<? extends String> getKnownLeases()
	{
		return new ArrayList<>(this.leases.keySet());
	}

	void release(String leaseName)
	{
		GrantedLease grantedLease = this.leases.get(leaseName);
		if (grantedLease == null)
		{
			return;
		}

		this.leaseService.release(grantedLease);
	}
}
