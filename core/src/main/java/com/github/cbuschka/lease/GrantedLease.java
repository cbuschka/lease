package com.github.cbuschka.lease;

import java.util.Date;
import java.util.Objects;

public class GrantedLease
{
	private final String name;

	private final String cookie;

	private final Date validUntil;

	public GrantedLease(String name, String cookie, Date validUntil)
	{
		name = Objects.requireNonNull(name);

		this.name = name;
		this.cookie = cookie;
		this.validUntil = validUntil;
	}

	public String getName()
	{
		return name;
	}

	public String getCookie()
	{
		return cookie;
	}

	public Date getValidUntil()
	{
		return validUntil;
	}

	@Override
	public int hashCode()
	{
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object otherObj)
	{
		if (otherObj == null)
		{
			return false;
		}

		if (this == otherObj)
		{
			return true;
		}

		GrantedLease other = (GrantedLease) otherObj;
		return this.name.equalsIgnoreCase(other.name)
				&& this.cookie.equals(other.cookie)
				&& this.validUntil.equals(other.validUntil);
	}

	@Override
	public String toString()
	{
		return String.format("%s{name=%s}", getClass().getSimpleName(), this.name);
	}
}

