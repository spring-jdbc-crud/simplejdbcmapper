package io.github.simplejdbcmapper.config;

import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.github.simplejdbcmapper.core.SimpleJdbcMapper;

@Component
public class SimpleJdbcMapperConfig {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Value("${sjm.runWithConversionServiceNull:false}")
	private boolean runWithConversionServiceNull;

	@Value("${sjm.runWithRecordAuditing:false}")
	private boolean runWithRecordAuditing;

	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	public SimpleJdbcMapper simpleJdbcMapper(DataSource dataSource) {
		SimpleJdbcMapper simpleJdbcMapper = null;
		if (jdbcDriver.contains("mysql")) {
			// schema1 database name for mysql
			simpleJdbcMapper = new SimpleJdbcMapper(dataSource, null, "schema1");
		} else {
			simpleJdbcMapper = new SimpleJdbcMapper(dataSource, "schema1");
		}

		// for testing with record auditing
		if (runWithRecordAuditing) {
			simpleJdbcMapper.setRecordAuditedBySupplier(() -> "tester");
			simpleJdbcMapper.setRecordAuditedOnSupplier(() -> LocalDateTime.now());
		}

		// just for testing purposes running without a conversionSevice
		if (runWithConversionServiceNull) {
			simpleJdbcMapper.setConversionService(null);
		}
		return simpleJdbcMapper;
	}

}
