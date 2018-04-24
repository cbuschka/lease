package com.github.cbuschka.lease.jdbc;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultJdbcLeaseDao implements JdbcLeaseDao
{
	private RowMapper<LeaseDto> leaseDtoRowMapper = new BeanPropertyRowMapper<>(LeaseDto.class);

	private ResultSetExtractor<List<LeaseDto>> leaseDtoListResultSetExtractor = new ResultSetExtractor<List<LeaseDto>>()
	{
		@Override
		public List<LeaseDto> extractData(ResultSet rs) throws SQLException, DataAccessException
		{
			List<LeaseDto> items = new ArrayList<>();
			for (int i = 0; rs.next(); ++i)
			{
				LeaseDto item = leaseDtoRowMapper.mapRow(rs, i);
				items.add(item);
			}
			return items;
		}
	};

	private String createSql = "create table t_lease (\n" +
			"  id number(38) primary key,\n" +
			"  name varchar(200) not null,\n" +
			"  cookie varchar(80) not null,\n" +
			"  holder varchar(80) not null,\n" +
			"  expires_at timestamp null\n" +
			")\n" +
			"/\n" +
			"create unique index idx_lease_name on t_lease ( lower(name) )\n" +
			"/\n" +
			"create sequence s_lease start with 10000\n" +
			"/";

	private String selectForUpdateSql = "select id, name, cookie, holder, expires_at, systimestamp as now from t_lease where lower(name) = lower(?) for update";

	private String selectSql = "select id, name, cookie, holder, expires_at, sysdate as now from t_lease where lower(name) = lower(?)";

	private String insertSql = "insert into t_lease ( id, name, cookie, holder, expires_at ) values ( s_lease.nextval, ?, ?, ?, (systimestamp + NUMTODSINTERVAL(?, 'SECOND')) )";

	private String updateSql = "update t_lease set cookie=?, holder=?, expires_at=(systimestamp + NUMTODSINTERVAL(?, 'SECOND')) where lower(name) = lower(?)";

	private String deleteSql = "delete from t_lease where lower(name) = lower(?) and cookie = ?";

	private DataSource dataSource;

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public void initSchema()
	{
		if (!isSchemaInitialized())
		{
			JdbcTemplate jdbcTemplate = newJdbcTemplate();
			for (String sql : this.createSql.split("/"))
			{
				jdbcTemplate.execute(sql);
			}
		}
	}

	private boolean isSchemaInitialized()
	{
		try
		{
			selectLease("");
			return true;
		}
		catch (DataAccessException ex)
		{
			return false;
		}
	}

	public Optional<LeaseDto> selectLeaseForUpdate(String leaseName)
	{
		return selectLeaseInternal(this.selectForUpdateSql, leaseName);
	}

	public void updateLease(String leaseName, String cookie, String holder, Duration validity)
	{
		newJdbcTemplate().update(this.updateSql, cookie, holder, validity.toMillis() / 1000, leaseName);
	}

	public void insertLease(String leaseName, String cookie, String holder, Duration validity)
	{
		newJdbcTemplate().update(this.insertSql, leaseName, cookie, holder, validity.toMillis() / 1000);
	}

	public Optional<LeaseDto> selectLease(String leaseName)
	{
		return selectLeaseInternal(this.selectSql, leaseName);
	}

	public void setSelectForUpdateSql(String selectForUpdateSql)
	{
		this.selectForUpdateSql = selectForUpdateSql;
	}

	public void setInsertSql(String insertSql)
	{
		this.insertSql = insertSql;
	}

	public void setLeaseDtoRowMapper(RowMapper<LeaseDto> leaseDtoRowMapper)
	{
		this.leaseDtoRowMapper = leaseDtoRowMapper;
	}

	public void setUpdateSql(String updateSql)
	{
		this.updateSql = updateSql;
	}

	public void setSelectSql(String selectSql)
	{
		this.selectSql = selectSql;
	}

	public void setDeleteSql(String deleteSql)
	{
		this.deleteSql = deleteSql;
	}

	private Optional<LeaseDto> selectLeaseInternal(String sql, Object... args)
	{
		List<LeaseDto> leaseObjects = newJdbcTemplate().query(sql, args, leaseDtoListResultSetExtractor);
		if (leaseObjects.isEmpty())
		{
			return Optional.empty();
		}

		return Optional.of(leaseObjects.get(0));
	}

	@Override
	public void deleteLease(String leaseName, String cookie)
	{
		newJdbcTemplate().update(this.deleteSql, new Object[]{leaseName, cookie});
	}

	private JdbcTemplate newJdbcTemplate()
	{
		return new JdbcTemplate(dataSource);
	}
}
