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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.util.Assert;

/**
 * CRUD methods and configuration for SimpleJdbcMapper.
 * 
 * <pre>
 * SimpleJdbcMapper should always be prepared in a Spring application context
 * and given to services as a bean reference. It maintains state, for example
 * caches table meta-data, insert/update SQL etc.
 * 
 * <b> Note: An instance of SimpleJdbcMapper is thread safe.</b>
 * </pre>
 * 
 * @author Antony Joseph
 */
public final class SimpleJdbcMapper {

	private final SimpleJdbcMapperSupport sjms;

	private final InsertHelper insertHelper;

	private final FindHelper findHelper;

	private final UpdateHelper updateHelper;

	private final DeleteHelper deleteHelper;

	private final TableMappingHelper tableMappingHelper;

	/**
	 * Constructor.
	 *
	 * @param dataSource the dataSource.
	 */
	public SimpleJdbcMapper(DataSource dataSource) {
		this(dataSource, null, null);
	}

	/**
	 * Constructor.
	 *
	 * @param dataSource the dataSource.
	 * @param schemaName the schema name.
	 */
	public SimpleJdbcMapper(DataSource dataSource, String schemaName) {
		this(dataSource, schemaName, null);
	}

	/**
	 * Constructor.
	 *
	 * @param dataSource  the dataSource.
	 * @param schemaName  the schema name.
	 * @param catalogName the catalog name.
	 */
	public SimpleJdbcMapper(DataSource dataSource, String schemaName, String catalogName) {
		Assert.notNull(dataSource, "dataSource must not be null");
		this.sjms = new SimpleJdbcMapperSupport(dataSource, schemaName, catalogName);
		this.tableMappingHelper = new TableMappingHelper(this.sjms);
		this.insertHelper = new InsertHelper(this.tableMappingHelper);
		this.findHelper = new FindHelper(this.tableMappingHelper);
		this.updateHelper = new UpdateHelper(this.tableMappingHelper);
		this.deleteHelper = new DeleteHelper(this.tableMappingHelper);

	}

	/**
	 * finds the object by Id. Returns null if not found
	 *
	 * @param <T>   the type
	 * @param clazz Class of object
	 * @param id    Id of object
	 * @return the object of type T
	 */
	public <T> T findById(Class<T> clazz, Object id) {
		return findHelper.findById(clazz, id);
	}

	/**
	 * Find all objects.
	 *
	 * @param <T>   the type
	 * @param clazz Type of object
	 * @return List of objects of type T
	 */
	public <T> List<T> findAll(Class<T> clazz) {
		return findHelper.findAll(clazz);
	}

	/**
	 * Inserts an object. Objects with auto generated id will have the id set to the
	 * new id from database. For non auto generated id the id has to be manually set
	 * before invoking insert().
	 *
	 * <pre>
	 * Will handle the following annotations:
	 * &#64;CreatedOn if Supplier is configured with SimpleJdbcMapper the property 
	 *                will be assigned the supplied value
	 * &#64;CreatedBy if Supplier is configured with SimpleJdbcMapper the property 
	 *      will be assigned the supplied value
	 * &#64;UpdatedOn if Supplier is configured with SimpleJdbcMapper the property 
	 *                will be assigned the supplied value
	 * &#64;UpdatedBy if Supplier is configured with SimpleJdbcMapper the property
	 *                will be assigned the supplied value
	 * &#64;Version property will be set to 1. Used for optimistic locking.
	 * </pre>
	 *
	 * @param obj The object to be saved
	 */

	public void insert(Object obj) {
		insertHelper.insert(obj);
	}

	/**
	 * Update the object.
	 *
	 * <pre>
	 * Will handle the following annotations:
	 * &#64;UpdatedOn if Supplier is configured with SimpleJdbcMapper the property 
	 *                will be assigned the supplied value
	 * &#64;UpdatedBy if Supplier is configured with SimpleJdbcMapper the property 
	 *                will be assigned the supplied value
	 * &#64;Version property will be incremented on a successful update. An OptimisticLockingException
	 *                will be thrown if object is stale.
	 * </pre>
	 *
	 * @param obj object to be updated
	 * @return number of records updated
	 */
	public Integer update(Object obj) {
		return updateHelper.update(obj);
	}

	/**
	 * Updates only the specified properties passed in as arguments. Use it to
	 * update a property or a few properties of the object and not the whole object.
	 * Issues an SQL update statement for only for the specific properties and any
	 * auto assign properties.
	 *
	 * <pre>
	 * Will handle the following annotations:
	 * &#64;UpdatedOn if Supplier is configured with SimpleJdbcMapper the property 
	 *                will be assigned the supplied value
	 * &#64;UpdatedBy if Supplier is configured with SimpleJdbcMapper the property 
	 *                will be assigned the supplied value
	 * &#64;Version property will be incremented on a successful update. An OptimisticLockingException
	 *                will be thrown if object is stale.
	 * </pre>
	 *
	 * @param obj           object to be updated
	 * @param propertyNames the specific property names that need to be updated.
	 * @return number of records updated
	 */
	public Integer updateSpecificProperties(Object obj, String... propertyNames) {
		return updateHelper.updateSpecificProperties(obj, propertyNames);
	}

	/**
	 * Deletes the object from the database.
	 *
	 * @param obj Object to be deleted
	 * @return number of records were deleted (1 or 0)
	 */
	public Integer delete(Object obj) {
		return deleteHelper.delete(obj);
	}

	/**
	 * Deletes the object from the database by id.
	 *
	 * @param clazz Type of object to be deleted.
	 * @param id    Id of object to be deleted
	 * @return number records were deleted (1 or 0)
	 */
	public Integer deleteById(Class<?> clazz, Object id) {
		return deleteHelper.deleteById(clazz, id);
	}

	/**
	 * Gets the columns SQL. Works well with Spring row mappers like
	 * BeanPropertyRowMapper(), SimplePropertyRowMapper() etc. Will create the
	 * needed column aliases when the column name does not match the corresponding
	 * underscore case property name.
	 *
	 * <p>
	 * Will return something like below if 'name' property is mapped to 'last_name'
	 * column in database:
	 *
	 * <pre>
	 * "somecolumn, someothercolumn, last_name AS name"
	 * </pre>
	 * 
	 * @param clazz the class
	 * @return comma separated select column string
	 * 
	 */
	public String getBeanFriendlySqlColumns(Class<?> clazz) {
		return findHelper.getBeanFriendlySqlColumns(clazz);
	}

	/**
	 * returns a map with all the properties of the mapped class and their
	 * corresponding column names
	 * 
	 * @param clazz the class
	 * @return map of property and their corresponding columns
	 * 
	 */
	public Map<String, String> getPropertyToColumnMappings(Class<?> clazz) {
		return findHelper.getPropertyToColumnMappings(clazz);
	}

	/**
	 * Gets the JdbcClient of the SimpleJdbcMapper.
	 *
	 * @return the JdbcClient
	 */
	public JdbcClient getJdbcClient() {
		return sjms.getJdbcClient();
	}

	/**
	 * Gets the JdbcTemplate of the SimpleJdbcMapper.
	 *
	 * @return the JdbcTemplate
	 */
	public JdbcTemplate getJdbcTemplate() {
		return sjms.getJdbcTemplate();
	}

	/**
	 * Gets the NamedParameterJdbcTemplate of the SimpleJdbcMapper.
	 *
	 * @return the NamedParameterJdbcTemplate
	 */
	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return sjms.getNamedParameterJdbcTemplate();
	}

	/**
	 * Set the Supplier that is used to populate the &#64;CreatedBy and
	 * &#64;UpdatedBy annotated properties.
	 * 
	 * @param <T>      the type
	 * @param supplier the Supplier for audited by.
	 */
	public <T> void setRecordAuditedBySupplier(Supplier<T> supplier) {
		sjms.setRecordAuditedOnSupplier(supplier);
	}

	/**
	 * Set the Supplier that is used to populate the &#64;CreatedOn and
	 * &#64;UpdatedOn annotated properties.
	 *
	 * @param <T>      the type
	 * @param supplier the Supplier for audited on.
	 */
	public <T> void setRecordAuditedOnSupplier(Supplier<T> supplier) {
		sjms.setRecordAuditedOnSupplier(supplier);
	}

	/**
	 * Exposing the conversion service used, so if necessary new converters can be
	 * added etc.
	 *
	 * @return the conversion service.
	 */
	public ConversionService getConversionService() {
		return sjms.getConversionService();
	}

	/**
	 * Set the conversion service
	 * 
	 * @param conversionService The conversion service to set
	 */
	public void setConversionService(ConversionService conversionService) {
		sjms.setConversionService(conversionService);
	}

	/**
	 * SimpleJdbcMapper depends on database meta data. Some drivers do not return
	 * correct java.sql.Types. For example some postgres drivers for column
	 * definition 'TIMESTAMP WITH TIMEZONE' return Types.TIMESTAMP which causes
	 * conversion failures when used with OffsetDateTime. This method overrides it
	 * to be Types.TIMESTAMP_WITH_TIMEZONE.
	 */
	public void enableOffsetDateTimeSqlTypeAsTimestampWithTimeZone() {
		sjms.enableOffsetDateTimeSqlTypeAsTimestampWithTimeZone();
	}

	/**
	 * Loads the mapping for a class. Mappings are lazy loaded ie they are loaded
	 * when the mapped object is used for the first time. This method is provided so
	 * that the mappings can be loaded during Spring application startup if needed.
	 *
	 * @param clazz the class
	 */
	public void loadMapping(Class<?> clazz) {
		tableMappingHelper.getTableMapping(clazz);
	}

	/**
	 * Get the schema name.
	 *
	 * @return the schema name.
	 */
	public String getSchemaName() {
		return sjms.getSchemaName();
	}

	/**
	 * Get the catalog name.
	 *
	 * @return the catalog name.
	 */
	public String getCatalogName() {
		return sjms.getCatalogName();
	}

	TableMapping getTableMapping(Class<?> clazz) {
		return tableMappingHelper.getTableMapping(clazz);
	}

	SimpleCache<String, TableMapping> getTableMappingCache() {
		return tableMappingHelper.getTableMappingCache();
	}

	SimpleCache<String, String> getFindByIdSqlCache() {
		return findHelper.getFindByIdSqlCache();
	}

	SimpleCache<String, SimpleJdbcInsert> getInsertSqlCache() {
		return insertHelper.getInsertSqlCache();
	}

	SimpleCache<String, SqlAndParams> getUpdateSqlCache() {
		return updateHelper.getUpdateSqlCache();
	}

	SimpleCache<String, SqlAndParams> getUpdateSpecificPropertiesSqlCache() {
		return updateHelper.getUpdateSpecificPropertiesSqlCache();
	}

	SimpleCache<String, String> getBeanColumnsSqlCache() {
		return findHelper.getBeanColumnsSqlCache();
	}

	SimpleJdbcMapperSupport getSimpleJdbcMapperSupport() {
		return sjms;
	}

	@SuppressWarnings("rawtypes")
	Supplier getRecordAuditedBySupplier() {
		return sjms.getRecordAuditedBySupplier();
	}

	@SuppressWarnings("rawtypes")
	Supplier getRecordAuditedOnSupplier() {
		return sjms.getRecordAuditedOnSupplier();
	}
}
