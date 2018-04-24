package com.github.cbuschka.lease;

import java.math.BigInteger;
import java.util.Random;

public class DefaultLeaseCookieGenerator implements LeaseCookieGenerator
{
	private static final Random random = new Random();

	@Override
	public String generateCookie()
	{
		byte[] bytes = new byte[16];
		random.nextBytes(bytes);
		return new BigInteger(bytes).abs().toString(16).toLowerCase();
	}
}
