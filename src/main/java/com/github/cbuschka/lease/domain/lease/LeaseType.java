package com.github.cbuschka.lease.domain.lease;

public enum LeaseType
{
	XXX_IMPORT(1000 * 60 * 5),
	YYY_IMPORT(1000 * 60 * 2),
	ZZZ_IMPORT(1000 * 60 * 5),
	AAA_USER_SYNC(1000 * 60 * 2),;

	private long validityDurationMillis;

	LeaseType(long validityDurationMillis)
	{
		this.validityDurationMillis = validityDurationMillis;
	}

	public long getValidityDurationMillis()
	{
		return validityDurationMillis;
	}
}
