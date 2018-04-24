package com.github.cbuschka.lease;

import com.github.cbuschka.lease.event.LeaseEventListener;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LeaseManagerConfiguration
{
	private Set<String> autoAcquiredLeases = new HashSet<>();

	private Duration intialRenewalDelay = Duration.ofSeconds(30);

	private Duration renewalDelay = Duration.ofSeconds(30);

	private Duration defaultLeaseValidity = Duration.ofMillis(1000 * 60 * 3);

	private boolean enabled = true;

	private List<LeaseEventListener> leaseEventListeners = new ArrayList<>();

	public Set<String> getAutoAcquiredLeases()
	{
		return autoAcquiredLeases;
	}

	public void addLeaseEventListener(LeaseEventListener listener)
	{
		this.leaseEventListeners.add(listener);
	}

	public List<LeaseEventListener> getLeaseEventListeners()
	{
		return leaseEventListeners;
	}

	public Duration getIntialRenewalDelay()
	{
		return intialRenewalDelay;
	}

	public Duration getRenewalDelay()
	{
		return renewalDelay;
	}

	public void setIntialRenewalDelay(Duration intialRenewalDelay)
	{
		this.intialRenewalDelay = intialRenewalDelay;
	}

	public void setRenewalDelay(Duration renewalDelay)
	{
		this.renewalDelay = renewalDelay;
	}

	public void setAutoAcquiredLeases(Set<String> autoAcquiredLeases)
	{
		this.autoAcquiredLeases = autoAcquiredLeases;
	}

	public Duration getDefaultLeaseValidity()
	{
		return defaultLeaseValidity;
	}

	public void setDefaultLeaseValidity(Duration defaultLeaseValidity)
	{
		this.defaultLeaseValidity = defaultLeaseValidity;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}
