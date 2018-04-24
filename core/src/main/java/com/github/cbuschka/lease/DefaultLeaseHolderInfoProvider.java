package com.github.cbuschka.lease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;

public class DefaultLeaseHolderInfoProvider implements LeaseHolderInfoProvider
{
	private static Logger logger = LoggerFactory.getLogger(DefaultLeaseHolderInfoProvider.class);

	private String holderInfo;

	public DefaultLeaseHolderInfoProvider()
	{
		this.holderInfo = generateHolderInfo();
	}

	@Override
	public String getHolderInfo()
	{
		return this.holderInfo;
	}

	private String generateHolderInfo()
	{
		String userName = System.getProperty("user.name", "no-user.name");

		String hostName = getHostName();

		String pid = getPid();

		return String.format("%s@%s(%s)", userName, hostName, pid);
	}

	private String getPid()
	{
		try
		{
			Path selfPath = new File("/proc/self").toPath().toRealPath();
			return selfPath.toFile().getName();
		}
		catch (IOException ex)
		{
			logger.warn("Could not determine pid.", ex);
			return "no-pid";
		}
	}

	private String getHostName()
	{
		try
		{
			return InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException ex)
		{
			return "localhost";
		}
	}
}
