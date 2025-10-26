package io.github.simplejdbcmapper.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
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

	@Value("${all.spring.flyway.locations}")
	private String locations;

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	@Qualifier("dsAll")
	private DataSource dsAll;

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
	public void flyWayDsAll() {
		if (jdbcDriver.contains("postgres")) {
			Flyway.configure().dataSource(dsAll).defaultSchema("schema2").locations(locations).load().migrate();

		} else {
			Flyway.configure().dataSource(dsAll).locations(locations).load().migrate();
		}
	}
}
