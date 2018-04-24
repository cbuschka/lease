package com.github.cbuschka.lease;

import java.time.Duration;

public interface LeaseService
{
	boolean isValid(GrantedLease lease);

	GrantedLease acquire(String lease) throws LeaseUnavailableException;

	GrantedLease acquire(String leaseName, Duration validity) throws LeaseUnavailableException;

	GrantedLease renew(GrantedLease lease) throws LeaseUnavailableException;

	GrantedLease renew(GrantedLease lease, Duration validity) throws LeaseUnavailableException;

	void release(GrantedLease lease);
}
