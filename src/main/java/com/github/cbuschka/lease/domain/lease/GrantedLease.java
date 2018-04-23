package com.github.cbuschka.lease.domain.lease;

import java.util.Date;

public class GrantedLease
{
	private LeaseType name;

	private String cookie;

	private Date validUntil;

	public GrantedLease(LeaseType name, String cookie, Date validUntil)
	{
		this.name = name;
		this.cookie = cookie;
		this.validUntil = validUntil;
	}

	public LeaseType getName()
	{
		return name;
	}

	public void setName(LeaseType name)
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

	public Date getValidUntil()
	{
		return validUntil;
	}

	public void setValidUntil(Date validUntil)
	{
		this.validUntil = validUntil;
	}
}

