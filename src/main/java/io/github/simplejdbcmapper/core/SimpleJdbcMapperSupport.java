/*
 * Copyright 2025 the original author or authors.
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

import java.util.function.Supplier;

import javax.sql.DataSource;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.Assert;

/**
 * Support class for SimpleJdbcMapper
 *
 * @author Antony Joseph
 */
class SimpleJdbcMapperSupport {
	private final DataSource dataSource;

	private final String schemaName;

	private final String catalogName;

	private final JdbcClient jdbcClient;

	private final JdbcTemplate jdbcTemplate;

	private final NamedParameterJdbcTemplate npJdbcTemplate;

	private final TableMappingProvider tableMappingProvider;

	private boolean enableOffsetDateTimeSqlTypeAsTimestampWithTimeZone = false;

	// Using Spring's DefaultConversionService as default conversionService for
	// SimpleJdbcMapper
	private ConversionService conversionService = DefaultConversionService.getSharedInstance();

	private Supplier<?> recordAuditedOnSupplier;

	private Supplier<?> recordAuditedBySupplier;

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
		this.jdbcClient = JdbcClient.create(jdbcTemplate);
		this.tableMappingProvider = new TableMappingProvider(this);
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

	@SuppressWarnings("rawtypes")
	public Supplier getRecordAuditedBySupplier() {
		return recordAuditedBySupplier;
	}

	@SuppressWarnings("rawtypes")
	public Supplier getRecordAuditedOnSupplier() {
		return recordAuditedOnSupplier;
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public void enableOffsetDateTimeSqlTypeAsTimestampWithTimeZone() {
		enableOffsetDateTimeSqlTypeAsTimestampWithTimeZone = true;
	}

	boolean isEnableOffsetDateTimeSqlTypeAsTimestampWithTimeZone() {
		return enableOffsetDateTimeSqlTypeAsTimestampWithTimeZone;
	}

	TableMapping getTableMapping(Class<?> clazz) {
		return tableMappingProvider.getTableMapping(clazz);
	}

	String getCommonDatabaseName() {
		return tableMappingProvider.getCommonDatabaseName();
	}

	BeanWrapper getBeanWrapper(Object obj) {
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(obj);
		bw.setConversionService(conversionService);
		return bw;
	}

	SimpleCache<String, TableMapping> getTableMappingCache() {
		return tableMappingProvider.getTableMappingCache();
	}

}
