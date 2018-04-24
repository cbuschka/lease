package com.github.cbuschka.lease.autoconfig;

import com.github.cbuschka.lease.config.LeaseManagerServiceBuilder;

public interface LeaseManagementConfigurer
{
	void configure(LeaseManagerServiceBuilder builder);
}
