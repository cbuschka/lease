package com.github.cbuschka.lease.autoconfig;

import com.github.cbuschka.lease.LeaseManagerConfiguration;

public interface LeaseManagerConfigurationCustomizer
{
	void customize(LeaseManagerConfiguration config);
}
