package com.github.cbuschka.lease;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultLeaseValidityRegistry implements LeaseValidityRegistry
{
	private static final Duration DEFAULT_VALIDITY = Duration.ofMinutes(3);

	private Duration defaultValidity = DEFAULT_VALIDITY;

	private final Map<String, Duration> validity = new ConcurrentHashMap<>();

	public void setDefaultValidity(Duration defaultValidity)
	{
		this.defaultValidity = defaultValidity;
	}

	public Duration getDefaultValidity()
	{
		return defaultValidity;
	}

	public void setValidityFor(String leaseName, Duration validity)
	{
		this.validity.put(leaseName, validity);
	}

	public Duration getValidityFor(String leaseName)
	{
		return this.validity.getOrDefault(leaseName, this.defaultValidity);
	}
}
