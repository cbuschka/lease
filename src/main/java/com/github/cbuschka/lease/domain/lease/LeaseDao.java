package com.github.cbuschka.lease.domain.lease;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class LeaseDao
{
	private String selectLeaseForUpdateSql = "select id, name, cookie, holder, expiry_at, systimestamp as now from t_lease where lower(name) = lower(?) for update";

	private String selectLeaseSql = "select id, name, cookie, holder, expiry_at, sysdate as now from t_lease where lower(name) = lower(?)";

	private String insertLeaseSql = "insert into t_lease ( id, name, cookie, holder, expiry_at ) values ( s_lease.nextval, ?, ?, ?, (systimestamp + NUMTODSINTERVAL(?, 'SECOND')) )";

	private String updateLeaseSql = "update t_lease set cookie=?, holder=?, expiry_at=(systimestamp + NUMTODSINTERVAL(?, 'SECOND')) where lower(name) = lower(?)";

	@Autowired
	private DataSource dataSource;

	public Optional<LeaseDto> selectLeaseForUpdate(String leaseName)
	{
		return selectLeaseInternal(this.selectLeaseForUpdateSql, leaseName);
	}

	public void updateLease(String leaseName, String cookie, String holder, long validityDurationMillis)
	{
		new JdbcTemplate(dataSource).update(this.updateLeaseSql, cookie, holder, validityDurationMillis / 1000, leaseName);
	}

	public void insertLease(String leaseName, String cookie, String holder, long validityDurationMillis)
	{
		new JdbcTemplate(dataSource).update(this.insertLeaseSql, leaseName, cookie, holder, validityDurationMillis / 1000);
	}

	public Optional<LeaseDto> selectLease(String leaseName)
	{
		return selectLeaseInternal(this.selectLeaseSql, leaseName);
	}

	private static final BeanPropertyRowMapper<LeaseDto> leaseDtoBeanPropertyRowMapper = new BeanPropertyRowMapper<>(LeaseDto.class);

	private Optional<LeaseDto> selectLeaseInternal(String sql, Object... args)
	{
		List<LeaseDto> leaseObjects = new JdbcTemplate(dataSource).query(sql, args, new ResultSetExtractor<List<LeaseDto>>()
		{
			@Override
			public List<LeaseDto> extractData(ResultSet rs) throws SQLException, DataAccessException
			{
				List<LeaseDto> items = new ArrayList<>();
				for (int i = 0; rs.next(); ++i)
				{
					LeaseDto item = leaseDtoBeanPropertyRowMapper.mapRow(rs, i);
					items.add(item);
				}
				return items;
			}
		});
		if (leaseObjects.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(leaseObjects.get(0));
	}
}
