package com.github.cbuschka.lease.autoconfig;

import com.github.cbuschka.lease.config.LeaseManagerServiceBuilder;

public interface LeaseManagerBuilderConfigurer
{
	void configure(LeaseManagerServiceBuilder builder);
}
