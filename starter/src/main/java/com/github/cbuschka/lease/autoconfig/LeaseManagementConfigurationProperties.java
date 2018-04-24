package com.github.cbuschka.lease.autoconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("lease")
public class LeaseManagementConfigurationProperties
{
	@Value("${enabled:true}")
	private boolean enabled;

	// ...

	public boolean isEnabled()
	{
		return enabled;
	}
}
