package io.github.simplejdbcmapper.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Every time the tests are run the database is reset (schema is dropped) and
 * migration scripts run again so we have a fresh set of tables and data.
 *
 * @author Antony Joseph
 */
@Component
public class FlywayMigrationConfig {

	@Value("${spring.flyway.locations}")
	private String locations;

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private DataSource dataSource;

	@Bean
	public static FlywayMigrationStrategy cleanMigrateStrategy() {
		return new FlywayMigrationStrategy() {
			@Override
			public void migrate(Flyway flyway) {
				flyway.clean();
				flyway.migrate();
			}
		};
	}

	@PostConstruct
	public void flyWay() {
		Flyway.configure().dataSource(dataSource).locations(locations).load().migrate();
	}
}
