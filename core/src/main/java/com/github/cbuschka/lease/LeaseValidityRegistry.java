package com.github.cbuschka.lease;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public interface LeaseValidityRegistry
{
	Duration getValidityFor(String leaseName);
}
