/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.simplejdbcmapper.core;

import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.support.SqlBinaryValue;
import org.springframework.jdbc.core.support.SqlCharacterValue;
import org.springframework.util.Assert;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.relationship.RelationshipMapper;

/**
 * Support class for SimpleJdbcMapper
 *
 * @author Antony Joseph
 */
class SimpleJdbcMapperSupport {

	private final DataSource dataSource;

	private final String schemaName;

	private final String catalogName;

	private JdbcClient jdbcClient;

	private final JdbcTemplate jdbcTemplate;

	private final NamedParameterJdbcTemplate npJdbcTemplate;

	private final TableMappingProvider tableMappingProvider;

	private final SimpleCache<Class<?>, String> entitySqlColumnsCache = new SimpleCache<>();

	// Using Spring's DefaultConversionService as conversionService for
	// SimpleJdbcMapper
	private ConversionService conversionService = new DefaultConversionService();

	private Supplier<?> recordAuditedOnSupplier;

	private Supplier<?> recordAuditedBySupplier;

	private boolean conversionServiceManuallySet = false;

	// Map key - classname-tableAlias
	// value - the column sql string
	private final SimpleCache<String, String> entitySqlColumnsAliasCache = new SimpleCache<>(5000);

	/**
	 * Constructor.
	 *
	 * @param jdbcClient  The jdbcClient
	 * @param schemaName  database schema name.
	 * @param catalogName database catalog name.
	 */
	public SimpleJdbcMapperSupport(DataSource dataSource, String schemaName, String catalogName) {
		Assert.notNull(dataSource, "dataSource must not be null");
		this.dataSource = dataSource;
		this.schemaName = schemaName;
		this.catalogName = catalogName;
		this.npJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		this.jdbcTemplate = npJdbcTemplate.getJdbcTemplate();
		this.jdbcClient = JdbcClient.create(npJdbcTemplate, conversionService);
		this.tableMappingProvider = new TableMappingProvider(schemaName, catalogName);
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public JdbcClient getJdbcClient() {
		return this.jdbcClient;
	}

	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return this.npJdbcTemplate;
	}

	public <T> void setRecordAuditedBySupplier(Supplier<T> supplier) {
		if (recordAuditedBySupplier == null) {
			recordAuditedBySupplier = supplier;
		} else {
			throw new IllegalStateException("recordAuditedBySupplier was already set and cannot be changed.");
		}
	}

	public <T> void setRecordAuditedOnSupplier(Supplier<T> supplier) {
		if (recordAuditedOnSupplier == null) {
			recordAuditedOnSupplier = supplier;
		} else {
			throw new IllegalStateException("recordAuditedOnSupplier was already set and cannot be changed.");
		}
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	public String getSqlColumns(Class<?> entityType) {
		Assert.notNull(entityType, "entityType must not be null");
		String columnsSql = entitySqlColumnsCache.get(entityType);
		if (columnsSql == null) {
			TableMapping tableMapping = getTableMapping(entityType);
			StringJoiner sj = new StringJoiner(", ", " ", " ");
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				sj.add(propMapping.getColumnName());
			}
			columnsSql = sj.toString();
			entitySqlColumnsCache.put(entityType, columnsSql);
		}
		return columnsSql;
	}

	public String getSqlColumns(Class<?> entityType, String tableAlias) {
		Assert.notNull(entityType, "entityType must not be null");
		InternalUtils.validateTableAlias(tableAlias);
		String cacheKey = entityType.getName() + "-" + tableAlias;
		String columnsSql = entitySqlColumnsAliasCache.get(cacheKey);
		if (columnsSql == null) {
			String tablePrefix = tableAlias + ".";
			TableMapping tableMapping = getTableMapping(entityType);
			StringJoiner sj = new StringJoiner(", ");
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				sj.add(tablePrefix + propMapping.getColumnName());
			}
			columnsSql = sj.toString();
			entitySqlColumnsAliasCache.put(cacheKey, columnsSql);
		}
		return columnsSql;
	}

	public String getSqlColumns(MultiEntity multiEntity) {
		Assert.notNull(multiEntity, "multiEntity must not be null");
		StringBuilder sb = new StringBuilder(256);
		int cnt = 0;
		for (Map.Entry<Class<?>, String> entry : multiEntity.getEntries()) {
			if (cnt > 0) {
				sb.append(", ");
			}
			sb.append(getSqlColumns(entry.getKey(), entry.getValue()));
			cnt++;
		}
		return sb.toString();
	}

	/**
	 * @deprecated Not needed anymore. See AttributeConverter
	 * @param conversionService
	 */
	@Deprecated(since = "2.5.0", forRemoval = true)
	public void setConversionService(ConversionService conversionService) {
		Assert.notNull(conversionService, "conversionService must not be null");
		if (conversionServiceManuallySet) {
			throw new IllegalStateException("conversionService was already set and cannot be changed.");
		} else {
			this.conversionService = conversionService;
			this.jdbcClient = JdbcClient.create(npJdbcTemplate, conversionService);
			conversionServiceManuallySet = true;
		}
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public String getFullyQualifiedTableName(Class<?> entityType) {
		TableMapping tableMapping = tableMappingProvider.getTableMapping(entityType);
		return tableMapping.fullyQualifiedTableName();
	}

	TableMapping getTableMapping(Class<?> entityType) {
		return tableMappingProvider.getTableMapping(entityType);
	}

	<T> EntityRowMapper<T> entityRowMapper(Class<T> entityType) {
		TableMapping tableMapping = getTableMapping(entityType);
		return new EntityRowMapper<>(tableMapping, conversionService, 1);
	}

	ResultSetExtractor<RelationshipMapper> resultSetExtractor(MultiEntity multiEntity) {
		return new RelationshipMapperExtractor(multiEntity, tableMappingProvider, conversionService);
	}

	@SuppressWarnings("rawtypes")
	Supplier getRecordAuditedBySupplier() {
		return recordAuditedBySupplier;
	}

	@SuppressWarnings("rawtypes")
	Supplier getRecordAuditedOnSupplier() {
		return recordAuditedOnSupplier;
	}

	SimpleCache<Class<?>, TableMapping> getTableMappingCache() {
		return tableMappingProvider.getTableMappingCache();
	}

	SimpleCache<Class<?>, String> getEntitySqlColumnsCache() {
		return entitySqlColumnsCache;
	}

	SimpleCache<String, String> getEntitySqlColumnsAliasCache() {
		return entitySqlColumnsAliasCache;
	}

	void addMapSqlParameterSource(MapSqlParameterSource mapSqlParameterSource, Object val, String paramName,
			PropertyMapping propMapping) {
		Integer columnSqlType = propMapping.getColumnSqlType();
		if (propMapping.isStandardAttribute()) {
			mapSqlParameterSource.addValue(paramName, val, columnSqlType);
		} else if (propMapping.isEnumAttribute()) {
			assignEnumMapSqlParameterSource(mapSqlParameterSource, val, paramName, columnSqlType);
		} else if (propMapping.isBlobAttribute()) {
			assignBlobMapSqlParameterSource(mapSqlParameterSource, val, paramName, columnSqlType);
		} else if (propMapping.isClobAttribute()) {
			assignClobMapSqlParameterSource(mapSqlParameterSource, val, paramName, columnSqlType);
		} else if (propMapping.isWithConverterAttribute()) {
			assignMapSqlParameterSourceWithConverter(mapSqlParameterSource, val, paramName, columnSqlType,
					propMapping.getConverter());
		}
	}

	void assignBlobMapSqlParameterSource(MapSqlParameterSource mapSqlParameterSource, Object val, String paramName,
			Integer columnSqlType) {
		if (val == null) {
			mapSqlParameterSource.addValue(paramName, null, columnSqlType);
		} else if (val instanceof byte[] byteArray) {
			mapSqlParameterSource.addValue(paramName, new SqlBinaryValue(byteArray), columnSqlType);
		} else {
			throw new MapperException(val.getClass().getSimpleName() + " " + paramName
					+ " : java type has to be byte[] for a BLOB mapping. No other type is supported.");
		}
	}

	void assignClobMapSqlParameterSource(MapSqlParameterSource mapSqlParameterSource, Object val, String paramName,
			Integer columnSqlType) {
		if (val == null) {
			mapSqlParameterSource.addValue(paramName, null, columnSqlType);
		} else if (val instanceof String str) {
			mapSqlParameterSource.addValue(paramName, new SqlCharacterValue(str), columnSqlType);
		} else {
			throw new MapperException(val.getClass().getSimpleName() + "." + paramName
					+ " : java type has to be String for a CLOB mapping. No other type is supported");
		}
	}

	void assignEnumMapSqlParameterSource(MapSqlParameterSource mapSqlParameterSource, Object val, String paramName,
			Integer columnSqlType) {
		if (val == null) {
			mapSqlParameterSource.addValue(paramName, null, columnSqlType);
		} else {
			mapSqlParameterSource.addValue(paramName, ((Enum<?>) val).name(), columnSqlType);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	void assignMapSqlParameterSourceWithConverter(MapSqlParameterSource mapSqlParameterSource, Object val,
			String paramName, Integer columnSqlType, AttributeConverter converter) {
		if (val == null) {
			mapSqlParameterSource.addValue(paramName, null, columnSqlType);
		} else {
			mapSqlParameterSource.addValue(paramName, converter.convertToDatabaseColumn(val), columnSqlType);
		}
	}
}
