package io.github.simplejdbcmapper.config;

import java.sql.Types;
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
		simpleJdbcMapper.setRecordOperatorResolver(new AppRecordOperatorResolver());

		// postgres metadata for column definition TIMESTAMP WITH TIMEZONE
		// Types.TIMESTAMP which is wrong which cause object convertion to fail.
		// So need to override.
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
		simpleJdbcMapper.setRecordOperatorResolver(new AppRecordOperatorResolver());
		return simpleJdbcMapper;
	}
}
