package com.github.cbuschka.lease.autoconfig;

import com.github.cbuschka.lease.LeaseManagerConfiguration;
import com.github.cbuschka.lease.LeaseManagerService;
import com.github.cbuschka.lease.config.LeaseManagerServiceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

public class LeaseManagementAutoConfiguration
{
	@Autowired(required = false)
	private List<LeaseManagerConfigurationCustomizer> configurationCustomizers;
	@Autowired(required = false)
	private List<LeaseManagerBuilderConfigurer> configurers;

	@Bean
	public LeaseManagerService leaseManagerService()
	{
		LeaseManagerConfiguration leaseManagerConfiguration = createLeaseManagerConfig();

		LeaseManagerServiceBuilder builder = configureLeaseManagementBuilder(leaseManagerConfiguration);

		return builder.build();
	}

	private LeaseManagerServiceBuilder configureLeaseManagementBuilder(LeaseManagerConfiguration leaseManagerConfiguration)
	{
		LeaseManagerServiceBuilder builder = new LeaseManagerServiceBuilder();
		builder.setLeaseManagerConfiguration(leaseManagerConfiguration);
		if (this.configurers != null)
		{
			for (LeaseManagerBuilderConfigurer configurer : this.configurers)
			{
				configurer.configure(builder);
			}
		}
		return builder;
	}

	private LeaseManagerConfiguration createLeaseManagerConfig()
	{
		LeaseManagerConfiguration leaseManagerConfiguration = new LeaseManagerConfiguration();
		if (configurationCustomizers != null)
		{
			for (LeaseManagerConfigurationCustomizer customizer : configurationCustomizers)
			{
				customizer.customize(leaseManagerConfiguration);
			}
		}
		return leaseManagerConfiguration;
	}

	@Configuration
	@ConditionalOnBean({DataSource.class})
	@ConditionalOnMissingBean(LeaseManagerBuilderConfigurer.class)
	public static class DefaultJdbcLeaseManagerBuilderConfigurer implements LeaseManagerBuilderConfigurer
	{
		@Autowired
		private DataSource dataSource;

		@Autowired
		private PlatformTransactionManager platformTransactionManager;

		@Override
		public void configure(LeaseManagerServiceBuilder builder)
		{
			builder.jdbc()
					.setDataSource(this.dataSource)
					.setPlatformTransactionManager(this.platformTransactionManager);
		}
	}
}
