package com.github.cbuschka.lease;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class DefaultLeaseCookieGeneratorTest
{
	private DefaultLeaseCookieGenerator defaultLeaseCookieGenerator = new DefaultLeaseCookieGenerator();

	@Test
	public void lowerCaseCookie()
	{
		String result = defaultLeaseCookieGenerator.generateCookie();

		assertThat(result.length(), not(is(0)));
		assertThat(result, is(result.toLowerCase()));
	}
}
