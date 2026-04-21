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

import java.util.Collection;
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
 * caches insert/update SQL etc.
 * 
 * <b> Note: An instance of SimpleJdbcMapper is thread safe once configured.</b>
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
	 * @param <T>        the type
	 * @param entityType the type of object
	 * @param id         id of object
	 * @return the object of type T
	 */
	public <T> T findById(Class<T> entityType, Object id) {
		return findOperation.findById(entityType, id);
	}

	/**
	 * Find all objects.
	 *
	 * @param <T>         the type
	 * @param entityType  type of object
	 * @param sortByArray optional argument. An array of SortBy objects that are
	 *                    used to generate the "ORDER BY" clause
	 * @return List of objects of type T
	 */
	public <T> List<T> findAll(Class<T> entityType, SortBy... sortByArray) {
		return findOperation.findAll(entityType, sortByArray);
	}

	/**
	 * Returns list of objects which match the property value. 'IS NULL' clause will
	 * be used in the sql for a null value.
	 *
	 * @param <T>           the type
	 * @param entityType    type of objects to be returned
	 * @param propertyName  the property name
	 * @param propertyValue the property value
	 * @param sortByArray   optional argument. An array of SortBy objects that are
	 *                      used to generate the "ORDER BY" clause
	 * @return a List of objects of type T
	 */
	public <T> List<T> findByPropertyValue(Class<T> entityType, String propertyName, Object propertyValue,
			SortBy... sortByArray) {
		return findOperation.findByPropertyValue(entityType, propertyName, propertyValue, sortByArray);
	}

	/**
	 * Returns list of objects which match the collection of property values. Uses
	 * an sql 'IN' clause. Large number of values could cause query performance
	 * degradation. Also different databases have different number/size limitations
	 * for sql 'IN" clauses.
	 * 
	 * <pre>
	 * Query is constructed in such a way that if there is a null value in the propertyValues
	 * the returned records will include records which match 'IS NULL' in the database.
	 * </pre>
	 *
	 * @param <T>            the type
	 * @param <U>            the type of the property values
	 * @param entityType     the type of objects to be returned
	 * @param propertyName   the property name
	 * @param propertyValues the collection of property values
	 * @param sortByArray    optional argument. An array of SortBy objects that are
	 *                       used to generate the "ORDER BY" clause
	 * @return a List of objects of type T
	 */
	public <T, U> List<T> findByPropertyValues(Class<T> entityType, String propertyName, Collection<U> propertyValues,
			SortBy... sortByArray) {
		return findOperation.findByPropertyValues(entityType, propertyName, propertyValues, sortByArray);
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
	 * @param object The object to be saved
	 */

	public void insert(Object object) {
		insertOperation.insert(object);
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
	 * @param object object to be updated
	 * @return number of records updated
	 */
	public Integer update(Object object) {
		return updateOperation.update(object);
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
	 * @param object        object to be updated
	 * @param propertyNames the specific property names that need to be updated.
	 * @return number of records updated
	 */
	public Integer updateSpecificProperties(Object object, String... propertyNames) {
		return updateOperation.updateSpecificProperties(object, propertyNames);
	}

	/**
	 * Deletes the object from the database.
	 *
	 * @param object Object to be deleted
	 * @return number of records were deleted (1 or 0)
	 */
	public Integer delete(Object object) {
		return deleteOperation.delete(object);
	}

	/**
	 * Deletes the object from the database by id.
	 *
	 * @param entityType type of object to be deleted.
	 * @param id         id of object to be deleted
	 * @return number records were deleted (1 or 0)
	 */
	public Integer deleteById(Class<?> entityType, Object id) {
		return deleteOperation.deleteById(entityType, id);
	}

	/**
	 * Gets the columns SQL. Works well with Spring row mappers like
	 * BeanPropertyRowMapper(), SimplePropertyRowMapper() etc. Will create the
	 * needed column aliases where the column name does not match the corresponding
	 * underscore case property name.
	 * <p>
	 * It is a good practice to store this string, since every time this method is
	 * invoked the columns have to be concatenated along with some string
	 * manipulation
	 *
	 * <p>
	 * Will return something like below if 'userLastName' property is mapped to
	 * 'last_name' column in database:
	 *
	 * <pre>
	 * "somecolumn, someothercolumn, last_name AS user_last_name"
	 * </pre>
	 * 
	 * @param entityType the type
	 * @return comma separated select column string
	 * 
	 */
	public String getBeanFriendlySqlColumns(Class<?> entityType) {
		return findOperation.getBeanFriendlySqlColumns(entityType);
	}

	/**
	 * Gets the columns SQL with columns prefixed with the table alias. Works well
	 * with Spring row mappers like BeanPropertyRowMapper(),
	 * SimplePropertyRowMapper() etc. Will prefix table column names with the
	 * 'tableAlias.' and will create the needed column aliases where the column name
	 * does not match the corresponding underscore case property name.
	 * <p>
	 * Use it in your custom queries when you are doing joins and need columns
	 * corresponding to a table alias.
	 *
	 * <p>
	 * It is a good practice to store this string, since every time this method is
	 * invoked the columns have to be concatenated long with some string
	 * manipulation.
	 * 
	 * <p>
	 * For tableAlias argument 't1' will return something like below if
	 * 'userLastName' property is mapped to 'last_name' column in database:
	 *
	 * <pre>
	 * "t1.somecolumn, t1.someothercolumn, t1.last_name AS user_last_name"
	 * </pre>
	 * 
	 * @param entityType the type
	 * @param tableAlias the table alias
	 * @return comma separated select column string
	 * 
	 */
	public String getBeanFriendlySqlColumns(Class<?> entityType, String tableAlias) {
		return findOperation.getBeanFriendlySqlColumns(entityType, tableAlias);
	}

	/**
	 * returns a map with all the properties of the mapped class and their
	 * corresponding column names
	 * 
	 * @param entityType the class
	 * @return map of property and their corresponding columns
	 * 
	 */
	public Map<String, String> getPropertyToColumnMappings(Class<?> entityType) {
		return findOperation.getPropertyToColumnMappings(entityType);
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
	 * added etc. The default conversion service used in SimpleJdbcMapper is
	 * Spring's DefaultConversionService.
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

	/**
	 * Closes down SimpleJdbcMapper. Clears all its internal caches and other
	 * references.
	 * <p>
	 * This is handled entirely for you by Spring. Upon the closing of its
	 * application context this method is automatically invoked
	 */
	public void close() {
		simpleJdbcMapperSupport.close();
		insertOperation.close();
		findOperation.close();
		updateOperation.close();
		deleteOperation.close();
	}

}
