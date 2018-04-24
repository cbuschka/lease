package com.github.cbuschka.lease;

import java.time.Duration;

public class LeaseUnavailableException extends RuntimeException
{
	public LeaseUnavailableException(String message)
	{
		super(message);
	}
}
