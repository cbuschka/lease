import com.github.cbuschka.lease.LeaseManagerConfiguration;
import com.github.cbuschka.lease.LeaseManagerService;
import com.github.cbuschka.lease.autoconfig.LeaseManagementAutoConfiguration;
import com.github.cbuschka.lease.autoconfig.LeaseManagerConfigurationCustomizer;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import java.time.Duration;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class LeaseAutoConfigurationTest
{
	private AnnotationConfigApplicationContext context;

	@After
	public void tearDown()
	{
		if (this.context != null)
		{
			this.context.close();
		}
	}

	@Test
	public void defaults()
	{
		load(DefaultsConfiguration.class, "");
		LeaseManagerService leaseService = this.context.getBean(LeaseManagerService.class);
		assertThat(leaseService, instanceOf(LeaseManagerService.class));
		assertThat(leaseService.isEnabled(), is(true));
	}

	@Test
	public void defaultsWithDataSource()
	{
		load(DefaultsWithDataSourceConfiguration.class, "");
		LeaseManagerService leaseService = this.context.getBean(LeaseManagerService.class);
		assertThat(leaseService, instanceOf(LeaseManagerService.class));
		assertThat(leaseService.isEnabled(), is(true));
	}

	private void load(Class<?> config, String... environment)
	{
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.register(config);
		applicationContext.register(LeaseManagementAutoConfiguration.class);
		applicationContext.refresh();
		this.context = applicationContext;
	}

	@Configuration
	static class DefaultsConfiguration
	{
	}

	@Configuration
	static class CustomLeaseManagementConfiguration
	{
		@Bean
		public LeaseManagerConfigurationCustomizer customize()
		{
			return new LeaseManagerConfigurationCustomizer()
			{
				@Override
				public void customize(LeaseManagerConfiguration config)
				{
				}
			};
		}
	}


	@Configuration
	static class DefaultsWithDataSourceConfiguration
	{
		@Bean
		public DataSource dataSource()
		{
			return new DriverManagerDataSource("jdbc:h2:", "sa", "");
		}

		@Bean
		public LeaseManagerConfigurationCustomizer customize()
		{
			return new LeaseManagerConfigurationCustomizer()
			{
				@Override
				public void customize(LeaseManagerConfiguration config)
				{
					config.setDefaultLeaseValidity(Duration.ofMillis(100));
					config.setIntialRenewalDelay(Duration.ofMillis(1));
					config.setRenewalDelay(Duration.ofMillis(5));
				}
			};
		}
	}
}