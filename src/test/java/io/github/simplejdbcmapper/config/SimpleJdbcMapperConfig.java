package io.github.simplejdbcmapper.config;

import java.sql.Types;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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

	@Primary
	@Bean(name = "ds1")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSourceDs1() {
		return DataSourceBuilder.create().build();
	}

	@Primary
	@Bean(name = "ds1SimpleJdbcMapper")
	public SimpleJdbcMapper ds1SimpleJdbcMapper(@Qualifier("ds1") DataSource dataSource) {
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
		// postgres data metadata for column definition 'TIMESTAMP WITH TIMEZONE'
		// returns java.sql.Types.TIMESTAMP which is wrong causing conversion failures
		// for OffsetDateTime. Override the database metadata for OffsetDateTime
		if (jdbcDriver.contains("postgres")) {
			Map<Class<?>, Integer> map = new HashMap<>();
			map.put(OffsetDateTime.class, Types.TIMESTAMP_WITH_TIMEZONE);
			simpleJdbcMapper.setDatabaseMetaDataOverride(map);
		}
		return simpleJdbcMapper;
	}

	@Bean(name = "dsAll")
	@ConfigurationProperties(prefix = "all.spring.datasource")
	public DataSource dataSourceAll() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	@Qualifier("allSimpleJdbcMapper")
	public SimpleJdbcMapper allSimpleJdbcMapper(@Qualifier("dsAll") DataSource dataSource) {
		SimpleJdbcMapper simpleJdbcMapper = new SimpleJdbcMapper(dataSource);
		simpleJdbcMapper.setRecordAuditedBySupplier(() -> "tester");
		return simpleJdbcMapper;
	}
}
