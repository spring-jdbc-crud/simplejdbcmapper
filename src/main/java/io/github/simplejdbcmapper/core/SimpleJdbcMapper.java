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

	private final SimpleJdbcMapperSupport simpleJdbcMapperSupport;

	private final InsertOperation insertOperation;

	private final FindOperation findOperation;

	private final UpdateOperation updateOperation;

	private final DeleteOperation deleteOperation;

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
		this.simpleJdbcMapperSupport = new SimpleJdbcMapperSupport(dataSource, schemaName, catalogName);
		this.insertOperation = new InsertOperation(simpleJdbcMapperSupport);
		this.findOperation = new FindOperation(simpleJdbcMapperSupport);
		this.updateOperation = new UpdateOperation(simpleJdbcMapperSupport);
		this.deleteOperation = new DeleteOperation(simpleJdbcMapperSupport);
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
		return findOperation.findById(clazz, id);
	}

	/**
	 * Find all objects.
	 *
	 * @param <T>   the type
	 * @param clazz Type of object
	 * @return List of objects of type T
	 */
	public <T> List<T> findAll(Class<T> clazz) {
		return findOperation.findAll(clazz);
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
		insertOperation.insert(obj);
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
		return updateOperation.update(obj);
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
		return updateOperation.updateSpecificProperties(obj, propertyNames);
	}

	/**
	 * Deletes the object from the database.
	 *
	 * @param obj Object to be deleted
	 * @return number of records were deleted (1 or 0)
	 */
	public Integer delete(Object obj) {
		return deleteOperation.delete(obj);
	}

	/**
	 * Deletes the object from the database by id.
	 *
	 * @param clazz Type of object to be deleted.
	 * @param id    Id of object to be deleted
	 * @return number records were deleted (1 or 0)
	 */
	public Integer deleteById(Class<?> clazz, Object id) {
		return deleteOperation.deleteById(clazz, id);
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
		return findOperation.getBeanFriendlySqlColumns(clazz);
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
		return findOperation.getPropertyToColumnMappings(clazz);
	}

	/**
	 * Gets the JdbcClient of the SimpleJdbcMapper.
	 *
	 * @return the JdbcClient
	 */
	public JdbcClient getJdbcClient() {
		return simpleJdbcMapperSupport.getJdbcClient();
	}

	/**
	 * Gets the JdbcTemplate of the SimpleJdbcMapper.
	 *
	 * @return the JdbcTemplate
	 */
	public JdbcTemplate getJdbcTemplate() {
		return simpleJdbcMapperSupport.getJdbcTemplate();
	}

	/**
	 * Gets the NamedParameterJdbcTemplate of the SimpleJdbcMapper.
	 *
	 * @return the NamedParameterJdbcTemplate
	 */
	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return simpleJdbcMapperSupport.getNamedParameterJdbcTemplate();
	}

	/**
	 * Set the Supplier that is used to populate the &#64;CreatedBy and
	 * &#64;UpdatedBy annotated properties.
	 * 
	 * @param <T>      the type
	 * @param supplier the Supplier for audited by.
	 */
	public <T> void setRecordAuditedBySupplier(Supplier<T> supplier) {
		simpleJdbcMapperSupport.setRecordAuditedBySupplier(supplier);
	}

	/**
	 * Set the Supplier that is used to populate the &#64;CreatedOn and
	 * &#64;UpdatedOn annotated properties.
	 *
	 * @param <T>      the type
	 * @param supplier the Supplier for audited on.
	 */
	public <T> void setRecordAuditedOnSupplier(Supplier<T> supplier) {
		simpleJdbcMapperSupport.setRecordAuditedOnSupplier(supplier);
	}

	/**
	 * Exposing the conversion service used, so if necessary new converters can be
	 * added etc.
	 *
	 * @return the conversion service.
	 */
	public ConversionService getConversionService() {
		return simpleJdbcMapperSupport.getConversionService();
	}

	/**
	 * Set the conversion service
	 * 
	 * @param conversionService The conversion service to set
	 */
	public void setConversionService(ConversionService conversionService) {
		simpleJdbcMapperSupport.setConversionService(conversionService);
	}

	/**
	 * Loads the mapping for a class. Mappings are lazy loaded ie they are loaded
	 * when the mapped object is used for the first time. This method is provided so
	 * that the mappings can be loaded during Spring application startup if needed.
	 *
	 * @param clazz the class
	 */
	public void loadMapping(Class<?> clazz) {
		simpleJdbcMapperSupport.getTableMapping(clazz);
	}

	/**
	 * Get the schema name.
	 *
	 * @return the schema name.
	 */
	public String getSchemaName() {
		return simpleJdbcMapperSupport.getSchemaName();
	}

	/**
	 * Get the catalog name.
	 *
	 * @return the catalog name.
	 */
	public String getCatalogName() {
		return simpleJdbcMapperSupport.getCatalogName();
	}
}
