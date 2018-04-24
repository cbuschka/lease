package com.github.cbuschka.lease.jdbc;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Optional;

public interface JdbcLeaseDao
{
	void setDataSource(DataSource dataSource);

	void initSchema();

	Optional<LeaseDto> selectLease(String leaseName);

	Optional<LeaseDto> selectLeaseForUpdate(String leaseName);

	void updateLease(String leaseName, String cookie, String holder, Duration validity);

	void insertLease(String leaseName, String cookie, String holder, Duration validity);

	void deleteLease(String leaseName, String cookie);
}
