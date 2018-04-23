package com.github.cbuschka.lease.domain.lease;

import java.util.Date;

public class LeaseDto
{
	private Long id;

	private String name;

	private String cookie;

	private String holder;

	private Date expiryAt;

	private Date now;

	public LeaseDto()
	{
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getCookie()
	{
		return cookie;
	}

	public void setCookie(String cookie)
	{
		this.cookie = cookie;
	}

	public String getHolder()
	{
		return holder;
	}

	public void setHolder(String holder)
	{
		this.holder = holder;
	}

	public Date getExpiryAt()
	{
		return expiryAt;
	}

	public void setExpiryAt(Date expiryAt)
	{
		this.expiryAt = expiryAt;
	}

	public Date getNow()
	{
		return now;
	}

	public void setNow(Date now)
	{
		this.now = now;
	}
}

