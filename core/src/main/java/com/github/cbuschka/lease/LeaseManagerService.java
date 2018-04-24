package com.github.cbuschka.lease;

public interface LeaseManagerService
{
	boolean isEnabled();

	// void acquire(String lease) throws LeaseUnavailableException;

	// void acquire(String lease, Duration validity) throws LeaseUnavailableException;

	// void release(String lease);

	boolean isAcquired(String lease);
}
