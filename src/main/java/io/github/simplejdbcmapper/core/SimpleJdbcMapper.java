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

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import javax.sql.DataSource;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcInsertOperations;
import org.springframework.jdbc.core.support.SqlBinaryValue;
import org.springframework.jdbc.core.support.SqlCharacterValue;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.Assert;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.exception.OptimisticLockingException;

/**
 * CRUD methods and configuration for SimpleJdbcMapper.
 *
 * Should be prepared in a Spring application context and given to services as
 * bean reference. SimpleJdbcMapper caches Table meta-data and SQL.
 * 
 * <b> Note: An instance of SimpleJdbcMapper is thread safe once
 * instantiated.</b>
 * 
 * <pre>
 * </pre>
 *
 * @author ajoseph
 */
public final class SimpleJdbcMapper {

	private static final int CACHEABLE_UPDATE_PROPERTIES_COUNT = 3;

	private final DataSource dataSource;

	private final JdbcClient jdbcClient;

	private final JdbcTemplate jdbcTemplate;

	private final NamedParameterJdbcTemplate npJdbcTemplate;

	private final SimpleJdbcMapperSupport simpleJdbcMapperSupport;

	private RecordOperatorResolver recordOperatorResolver;

	// insert cache. Note that Spring SimpleJdbcInsert is thread safe.
	// Map key - class name
	// value - SimpleJdbcInsert
	private SimpleCache<String, SimpleJdbcInsert> insertSqlCache = new SimpleCache<>();

	// update sql cache
	// Map key - class name
	// value - the update sql and params
	private SimpleCache<String, SqlAndParams> updateSqlCache = new SimpleCache<>();

	// update specified properties sql cache
	// Map key - class name and properties
	// value - the update sql and params
	private SimpleCache<String, SqlAndParams> updateSpecificPropertiesSqlCache = new SimpleCache<>(2000);

	// the column sql string with bean friendly column aliases for mapped properties
	// of model.
	// Map key - class name
	// value - the column sql string
	private SimpleCache<String, String> beanColumnsSqlCache = new SimpleCache<>();

	private SimpleCache<String, String> findByIdSqlCache = new SimpleCache<>();

	private ConversionService conversionService = null;

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
	 * @param schemaName database schema name.
	 */
	public SimpleJdbcMapper(DataSource dataSource, String schemaName) {
		this(dataSource, schemaName, null);
	}

	/**
	 * Constructor.
	 *
	 * @param dataSource  the dataSource
	 * @param schemaName  database schema name.
	 * @param catalogName database catalog name.
	 */
	public SimpleJdbcMapper(DataSource dataSource, String schemaName, String catalogName) {
		Assert.notNull(dataSource, "dataSource must not be null");
		this.dataSource = dataSource;
		this.jdbcClient = JdbcClient.create(dataSource);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.npJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		this.simpleJdbcMapperSupport = new SimpleJdbcMapperSupport(dataSource, schemaName, catalogName);
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
		Assert.notNull(clazz, "Class must not be null");
		TableMapping tableMapping = simpleJdbcMapperSupport.getTableMapping(clazz);
		boolean foundInCache = false;
		String sql = findByIdSqlCache.get(clazz.getName());
		if (sql == null) {
			sql = "SELECT " + getBeanColumnsSql(tableMapping, clazz) + " FROM " + tableMapping.fullyQualifiedTableName()
					+ " WHERE " + tableMapping.getIdColumnName() + " = ?";
		} else {
			foundInCache = true;
		}
		BeanPropertyRowMapper<T> rowMapper = getBeanPropertyRowMapper(clazz);
		T obj = null;
		try {
			obj = jdbcTemplate.queryForObject(sql, rowMapper,
					new SqlParameterValue(tableMapping.getIdColumnSqlType(), id));
		} catch (EmptyResultDataAccessException e) {
			// do nothing
		}

		if (!foundInCache && obj != null) {
			findByIdSqlCache.put(clazz.getName(), sql);
		}
		return obj;
	}

	/**
	 * Find all objects.
	 *
	 * @param <T>   the type
	 * @param clazz Type of object
	 * @return List of objects of type T
	 */
	public <T> List<T> findAll(Class<T> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		TableMapping tableMapping = simpleJdbcMapperSupport.getTableMapping(clazz);

		String sql = "SELECT " + getBeanColumnsSql(tableMapping, clazz) + " FROM "
				+ tableMapping.fullyQualifiedTableName();

		BeanPropertyRowMapper<T> rowMapper = getBeanPropertyRowMapper(clazz);
		return jdbcTemplate.query(sql, rowMapper);
	}

	/**
	 * Inserts an object. Objects with auto increment id will have the id set to the
	 * new id from database. For non auto increment id the id has to be manually set
	 * before invoking insert().
	 *
	 * <pre>
	 * Will handle the following annotations:
	 * &#64;CreatedOn property will be assigned current date and time
	 * &#64;CreatedBy if RecordOperatorResolver is configured with SimpleJdbcMapper the property 
	 *      will be assigned that value
	 * &#64;UpdatedOn property will be assigned current date and time
	 * &#64;UpdatedBy if RecordOperatorResolver is configured with SimpleJdbcMapper the property
	 *                will be assigned that value
	 * &#64;Version property will be set to 1. Used for optimistic locking.
	 * </pre>
	 *
	 * @param obj The object to be saved
	 */

	public void insert(Object obj) {
		Assert.notNull(obj, "Object must not be null");
		TableMapping tableMapping = simpleJdbcMapperSupport.getTableMapping(obj.getClass());
		BeanWrapper bw = getBeanWrapper(obj);
		Object idValue = bw.getPropertyValue(tableMapping.getIdPropertyName());
		if (tableMapping.isIdAutoGenerated()) {
			if (idValue != null) {
				throw new MapperException("For insert() the property " + obj.getClass().getSimpleName() + "."
						+ tableMapping.getIdPropertyName()
						+ " has to be null since this insert is for an object whose id is auto generated");
			}
		} else {
			if (idValue == null) {
				throw new MapperException("For insert() the property " + obj.getClass().getSimpleName() + "."
						+ tableMapping.getIdPropertyName() + " must not be null since it is not an auto generated id");
			}
		}
		if (tableMapping.hasAutoAssignProperties()) {
			LocalDateTime now = LocalDateTime.now();
			PropertyMapping createdOnPropMapping = tableMapping.getCreatedOnPropertyMapping();
			if (createdOnPropMapping != null) {
				bw.setPropertyValue(createdOnPropMapping.getPropertyName(), now);
			}
			PropertyMapping updatedOnPropMapping = tableMapping.getUpdatedOnPropertyMapping();
			if (updatedOnPropMapping != null) {
				bw.setPropertyValue(updatedOnPropMapping.getPropertyName(), now);
			}
			PropertyMapping createdByPropMapping = tableMapping.getCreatedByPropertyMapping();
			if (createdByPropMapping != null && recordOperatorResolver != null) {
				bw.setPropertyValue(createdByPropMapping.getPropertyName(), recordOperatorResolver.getRecordOperator());
			}
			PropertyMapping updatedByPropMapping = tableMapping.getUpdatedByPropertyMapping();
			if (updatedByPropMapping != null && recordOperatorResolver != null) {
				bw.setPropertyValue(updatedByPropMapping.getPropertyName(), recordOperatorResolver.getRecordOperator());
			}
			PropertyMapping versionPropMapping = tableMapping.getVersionPropertyMapping();
			if (versionPropMapping != null) {
				// version property value defaults to 1 on inserts
				bw.setPropertyValue(versionPropMapping.getPropertyName(), 1);
			}
		}
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
			if (propMapping.getColumnSqlDataType() == Types.BLOB) {
				if (bw.getPropertyValue(propMapping.getPropertyName()) == null) {
					mapSqlParameterSource.addValue(propMapping.getColumnName(), null);
				} else {
					mapSqlParameterSource.addValue(propMapping.getColumnName(),
							new SqlBinaryValue((byte[]) bw.getPropertyValue(propMapping.getPropertyName())));
				}
			} else if (propMapping.getColumnSqlDataType() == Types.CLOB) {
				if (bw.getPropertyValue(propMapping.getPropertyName()) == null) {
					mapSqlParameterSource.addValue(propMapping.getColumnName(), null);
				} else {
					mapSqlParameterSource.addValue(propMapping.getColumnName(),
							new SqlCharacterValue((char[]) bw.getPropertyValue(propMapping.getPropertyName())));
				}
			} else {
				if (simpleJdbcMapperSupport.getDatabaseMetaDataOverride(propMapping.getPropertyType()) != null) {
					mapSqlParameterSource.addValue(propMapping.getColumnName(),
							bw.getPropertyValue(propMapping.getPropertyName()),
							simpleJdbcMapperSupport.getDatabaseMetaDataOverride(propMapping.getPropertyType()));
				} else {
					mapSqlParameterSource.addValue(propMapping.getColumnName(),
							bw.getPropertyValue(propMapping.getPropertyName()));
				}
			}
		}
		boolean foundInCache = false;
		SimpleJdbcInsertOperations jdbcInsert = insertSqlCache.get(obj.getClass().getName());
		if (jdbcInsert == null) {
			jdbcInsert = new SimpleJdbcInsert(dataSource).withCatalogName(tableMapping.getCatalogName())
					.withSchemaName(tableMapping.getSchemaName()).withTableName(tableMapping.getTableName());
			if (tableMapping.isIdAutoGenerated()) {
				jdbcInsert.usingGeneratedKeyColumns(tableMapping.getIdColumnName());
			}
			// for oracle synonym table metadata
			if ("oracle".equalsIgnoreCase(simpleJdbcMapperSupport.getCommonDatabaseName())) {
				jdbcInsert.includeSynonymsForTableColumnMetaData();
			}
		} else {
			foundInCache = true;
		}
		if (tableMapping.isIdAutoGenerated()) {
			KeyHolder kh = jdbcInsert.executeAndReturnKeyHolder(mapSqlParameterSource);
			Object generatedId = null;
			if (Number.class.isAssignableFrom(tableMapping.getIdPropertyType())) {
				generatedId = kh.getKeyAs(Number.class);
			} else {
				generatedId = kh.getKeyAs(tableMapping.getIdPropertyType());
			}
			bw.setPropertyValue(tableMapping.getIdPropertyName(), generatedId);
		} else {
			jdbcInsert.execute(mapSqlParameterSource);
		}
		if (!foundInCache) {
			// SimpleJdbcInsert is thread safe.
			insertSqlCache.put(obj.getClass().getName(), (SimpleJdbcInsert) jdbcInsert);
		}
	}

	/**
	 * Update the object.
	 *
	 * <pre>
	 * Will handle the following annotations:
	 * &#64;UpdatedOn property will be assigned current date and time
	 * &#64;UpdatedBy if RecordOperatorResolver is configured with SimpleJdbcMapper the property 
	 *                will be assigned that value
	 * &#64;Version property will be incremented on a successful update. An OptimisticLockingException
	 *                will be thrown if object is stale.
	 * </pre>
	 *
	 * @param obj object to be updated
	 * @return number of records updated
	 */
	public Integer update(Object obj) {
		Assert.notNull(obj, "Object must not be null");
		TableMapping tableMapping = simpleJdbcMapperSupport.getTableMapping(obj.getClass());
		boolean foundInCache = false;
		SqlAndParams sqlAndParams = updateSqlCache.get(obj.getClass().getName());
		if (sqlAndParams == null) {
			sqlAndParams = buildSqlAndParamsForUpdate(tableMapping);
		} else {
			foundInCache = true;
		}
		Integer cnt = updateInternal(obj, sqlAndParams, tableMapping);

		if (!foundInCache && cnt > 0) {
			updateSqlCache.put(obj.getClass().getName(), sqlAndParams);
		}
		return cnt;
	}

	/**
	 * Updates the specified properties passed in as arguments. Use it to update a
	 * property or a few properties of the object and not the whole object. Issues
	 * an SQL update statement for only for the specific properties and any auto
	 * assign properties.
	 *
	 * <pre>
	 * Will handle the following annotations:
	 * &#64;UpdatedOn property will be assigned current date and time
	 * &#64;UpdatedBy if RecordOperatorResolver is configured with SimpleJdbcMapper the property 
	 *                will be assigned that value
	 * &#64;Version property will be incremented on a successful update. An OptimisticLockingException
	 *                will be thrown if object is stale.
	 * </pre>
	 *
	 * @param obj           object to be updated
	 * @param propertyNames the specific property names that need to be updated.
	 * @return number of records updated
	 */
	public Integer updateSpecificProperties(Object obj, String... propertyNames) {
		Assert.notNull(obj, "Object must not be null");
		Assert.notNull(propertyNames, "propertyNames must not be null");
		TableMapping tableMapping = simpleJdbcMapperSupport.getTableMapping(obj.getClass());
		boolean foundInCache = false;
		SqlAndParams sqlAndParams = null;
		String cacheKey = getUpdateSpecificPropertiesCacheKey(obj, propertyNames);
		if (cacheKey != null) {
			sqlAndParams = updateSpecificPropertiesSqlCache.get(cacheKey);
		}
		if (sqlAndParams == null) {
			sqlAndParams = buildSqlAndParamsForUpdateProperties(tableMapping, propertyNames);
		} else {
			foundInCache = true;
		}
		Integer cnt = updateInternal(obj, sqlAndParams, tableMapping);
		if (cacheKey != null && !foundInCache && cnt > 0) {
			updateSpecificPropertiesSqlCache.put(cacheKey, sqlAndParams);
		}
		return cnt;
	}

	private Integer updateInternal(Object obj, SqlAndParams sqlAndParams, TableMapping tableMapping) {
		Assert.notNull(obj, "Object must not be null");
		Assert.notNull(sqlAndParams, "sqlAndParams must not be null");
		BeanWrapper bw = getBeanWrapper(obj);
		if (bw.getPropertyValue(tableMapping.getIdPropertyName()) == null) {
			throw new IllegalArgumentException("Property " + tableMapping.getTableClassName() + "."
					+ tableMapping.getIdPropertyName() + " is the id and must not be null.");
		}
		Set<String> parameters = sqlAndParams.getParams();
		if (tableMapping.hasAutoAssignProperties()) {
			PropertyMapping updatedByPropMapping = tableMapping.getUpdatedByPropertyMapping();
			if (updatedByPropMapping != null && recordOperatorResolver != null
					&& parameters.contains(updatedByPropMapping.getPropertyName())) {
				bw.setPropertyValue(updatedByPropMapping.getPropertyName(), recordOperatorResolver.getRecordOperator());
			}
			PropertyMapping updatedOnPropMapping = tableMapping.getUpdatedOnPropertyMapping();
			if (updatedOnPropMapping != null && parameters.contains(updatedOnPropMapping.getPropertyName())) {
				bw.setPropertyValue(updatedOnPropMapping.getPropertyName(), LocalDateTime.now());
			}
		}
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		for (String paramName : parameters) {
			if (paramName.equals("incrementedVersion")) {
				Integer versionVal = (Integer) bw
						.getPropertyValue(tableMapping.getVersionPropertyMapping().getPropertyName());
				if (versionVal == null) {
					throw new MapperException(obj.getClass().getSimpleName() + "."
							+ tableMapping.getVersionPropertyMapping().getPropertyName()
							+ " is configured with annotation @Version. Property "
							+ tableMapping.getVersionPropertyMapping().getPropertyName()
							+ " must not be null when updating.");
				} else {
					mapSqlParameterSource.addValue("incrementedVersion", versionVal + 1, java.sql.Types.INTEGER);
				}
			} else {
				if (tableMapping.getPropertySqlType(paramName) == Types.BLOB) {
					if (bw.getPropertyValue(paramName) == null) {
						mapSqlParameterSource.addValue(paramName, null, tableMapping.getPropertySqlType(paramName));
					} else {
						mapSqlParameterSource.addValue(paramName,
								new SqlBinaryValue((byte[]) bw.getPropertyValue(paramName)),
								tableMapping.getPropertySqlType(paramName));
					}
				} else if (tableMapping.getPropertySqlType(paramName) == Types.CLOB) {
					if (bw.getPropertyValue(paramName) == null) {
						mapSqlParameterSource.addValue(paramName, null, tableMapping.getPropertySqlType(paramName));
					} else {
						mapSqlParameterSource.addValue(paramName,
								new SqlCharacterValue((char[]) bw.getPropertyValue(paramName)),
								tableMapping.getPropertySqlType(paramName));
					}
				} else {
					if (tableMapping.getOverriddenPropertySqlType(paramName) != null) {
						mapSqlParameterSource.addValue(paramName, bw.getPropertyValue(paramName),
								tableMapping.getOverriddenPropertySqlType(paramName));
					} else {
						mapSqlParameterSource.addValue(paramName, bw.getPropertyValue(paramName),
								tableMapping.getPropertySqlType(paramName));
					}
				}
			}
		}
		int cnt = -1;
		// if object has property version the version gets incremented on update.
		// throws OptimisticLockingException when update fails.
		if (sqlAndParams.getParams().contains("incrementedVersion")) {
			cnt = npJdbcTemplate.update(sqlAndParams.getSql(), mapSqlParameterSource);
			if (cnt == 0) {
				throw new OptimisticLockingException(obj.getClass().getSimpleName()
						+ " update failed due to stale data. Failed for " + tableMapping.getIdColumnName() + " = "
						+ bw.getPropertyValue(tableMapping.getIdPropertyName()) + " and "
						+ tableMapping.getVersionPropertyMapping().getColumnName() + " = "
						+ bw.getPropertyValue(tableMapping.getVersionPropertyMapping().getPropertyName()));
			}
			// update the version in object with new version
			bw.setPropertyValue(tableMapping.getVersionPropertyMapping().getPropertyName(),
					mapSqlParameterSource.getValue("incrementedVersion"));
		} else {
			cnt = npJdbcTemplate.update(sqlAndParams.getSql(), mapSqlParameterSource);
		}
		return cnt;
	}

	/**
	 * Deletes the object from the database.
	 *
	 * @param obj Object to be deleted
	 * @return number of records were deleted (1 or 0)
	 */
	public Integer delete(Object obj) {
		Assert.notNull(obj, "Object must not be null");
		TableMapping tableMapping = simpleJdbcMapperSupport.getTableMapping(obj.getClass());
		String sql = "DELETE FROM " + tableMapping.fullyQualifiedTableName() + " WHERE "
				+ tableMapping.getIdColumnName() + "= ?";
		BeanWrapper bw = getBeanWrapper(obj);
		Object id = bw.getPropertyValue(tableMapping.getIdPropertyName());
		return jdbcTemplate.update(sql, id);
	}

	/**
	 * Deletes the object from the database by id.
	 *
	 * @param clazz Type of object to be deleted.
	 * @param id    Id of object to be deleted
	 * @return number records were deleted (1 or 0)
	 */
	public Integer deleteById(Class<?> clazz, Object id) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(id, "id must not be null");
		TableMapping tableMapping = simpleJdbcMapperSupport.getTableMapping(clazz);
		String sql = "DELETE FROM " + tableMapping.fullyQualifiedTableName() + " WHERE "
				+ tableMapping.getIdColumnName() + " = ?";
		return jdbcTemplate.update(sql, id);
	}

	/**
	 * Gets the sql for the columns. Works well with Spring row mappers like
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
		return getBeanColumnsSql(simpleJdbcMapperSupport.getTableMapping(clazz), clazz);
	}

	/**
	 * returns a map with all the properties of the class with their corresponding
	 * column names
	 * 
	 * @param clazz the class
	 * @return map of property and their corresponding columns
	 * 
	 */
	public Map<String, String> getPropertyToColumnMappings(Class<?> clazz) {
		TableMapping tableMapping = simpleJdbcMapperSupport.getTableMapping(clazz);
		Map<String, String> map = new LinkedHashMap<>();
		for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
			map.put(propMapping.getPropertyName(), propMapping.getColumnName());
		}
		return map;
	}

	/**
	 * Loads the mapping for a class. Model mappings are loaded when they are used
	 * for the first time. This method is provided so that the mappings can be
	 * loaded during Spring application startup if needed.
	 *
	 * @param clazz the class
	 */
	public void loadMapping(Class<?> clazz) {
		simpleJdbcMapperSupport.getTableMapping(clazz);
	}

	/**
	 * Gets the JdbcClient of the SimpleJdbcMapper.
	 *
	 * @return the JdbcClient
	 */
	public JdbcClient getJdbcClient() {
		return this.jdbcClient;
	}

	/**
	 * Gets the JdbcTemplate of the SimpleJdbcMapper.
	 *
	 * @return the JdbcTemplate
	 */
	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return this.npJdbcTemplate;
	}

	/**
	 * An implementation of RecordOperatorResolver is used to populate the
	 * &#64;CreatedBy and &#64;UpdatedBy annotated properties.
	 *
	 * @param recordOperatorResolver The implement for interface
	 *                               RecordOperatorResolver
	 */
	public void setRecordOperatorResolver(RecordOperatorResolver recordOperatorResolver) {
		if (this.recordOperatorResolver == null) {
			this.recordOperatorResolver = recordOperatorResolver;
		} else {
			throw new IllegalStateException("recordOperatorResolver was already set and cannot be changed.");
		}
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
	 * Exposing the conversion service used so if necessary new converters can be
	 * added.
	 *
	 * @return the default conversion service.
	 */
	public ConversionService getConversionService() {
		return conversionService;
	}

	/**
	 * Set the conversion service
	 * 
	 * @param conversionService The conversion service to set
	 */
	public void setConversionService(ConversionService conversionService) {
		if (this.conversionService == null) {
			this.conversionService = conversionService;
		} else {
			throw new IllegalStateException("conversionService was already set and cannot be changed.");
		}
	}

	TableMapping getTableMapping(Class<?> clazz) {
		return simpleJdbcMapperSupport.getTableMapping(clazz);
	}

	private SqlAndParams buildSqlAndParamsForUpdate(TableMapping tableMapping) {
		Assert.notNull(tableMapping, "tableMapping must not be null");
		// ignore these attributes when generating the sql 'SET' command
		List<String> ignoreAttrs = new ArrayList<>();
		ignoreAttrs.add(tableMapping.getIdPropertyName());
		PropertyMapping createdOnPropMapping = tableMapping.getCreatedOnPropertyMapping();
		if (createdOnPropMapping != null) {
			ignoreAttrs.add(createdOnPropMapping.getPropertyName());
		}
		PropertyMapping createdByPropMapping = tableMapping.getCreatedByPropertyMapping();
		if (createdByPropMapping != null) {
			ignoreAttrs.add(createdByPropMapping.getPropertyName());
		}
		Set<String> params = new HashSet<>();
		StringBuilder sqlBuilder = new StringBuilder("UPDATE ");
		sqlBuilder.append(tableMapping.fullyQualifiedTableName());
		sqlBuilder.append(" SET ");
		PropertyMapping versionPropMapping = tableMapping.getVersionPropertyMapping();
		boolean first = true;
		for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
			if (ignoreAttrs.contains(propMapping.getPropertyName())) {
				continue;
			}
			if (!first) {
				sqlBuilder.append(", ");
			} else {
				first = false;
			}
			sqlBuilder.append(propMapping.getColumnName());
			sqlBuilder.append(" = :");
			if (versionPropMapping != null
					&& propMapping.getPropertyName().equals(versionPropMapping.getPropertyName())) {
				sqlBuilder.append("incrementedVersion");
				params.add("incrementedVersion");
			} else {
				sqlBuilder.append(propMapping.getPropertyName());
				params.add(propMapping.getPropertyName());
			}
		}
		// the where clause
		sqlBuilder.append(" WHERE " + tableMapping.getIdColumnName() + " = :" + tableMapping.getIdPropertyName());
		params.add(tableMapping.getIdPropertyName());
		if (versionPropMapping != null) {
			sqlBuilder.append(" AND ").append(versionPropMapping.getColumnName()).append(" = :")
					.append(versionPropMapping.getPropertyName());
			params.add(versionPropMapping.getPropertyName());
		}
		String updateSql = sqlBuilder.toString();
		return new SqlAndParams(updateSql, params);
	}

	private SqlAndParams buildSqlAndParamsForUpdateProperties(TableMapping tableMapping, String... propertyNames) {
		Assert.notNull(tableMapping, "tableMapping must not be null");
		Assert.notNull(propertyNames, "propertyNames must not be null");
		// do the validations
		for (String propertyName : propertyNames) {
			PropertyMapping propertyMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
			if (propertyMapping == null) {
				throw new MapperException("No mapping found for property '" + propertyName + "' in class "
						+ tableMapping.getTableClassName());
			}
			// id property cannot be updated
			if (propertyMapping.isIdAnnotation()) {
				throw new MapperException(
						"Id property " + tableMapping.getTableClassName() + "." + propertyName + " cannot be updated.");
			}
			// auto assign properties cannot be updated
			if (propertyMapping.isCreatedByAnnotation() || propertyMapping.isCreatedOnAnnotation()
					|| propertyMapping.isUpdatedByAnnotation() || propertyMapping.isUpdatedOnAnnotation()
					|| propertyMapping.isVersionAnnotation()) {
				throw new MapperException("Auto assign property " + tableMapping.getTableClassName() + "."
						+ propertyName + " cannot be updated.");
			}
		}
		Set<String> params = new HashSet<>();
		StringBuilder sqlBuilder = new StringBuilder("UPDATE ");
		sqlBuilder.append(tableMapping.fullyQualifiedTableName());
		sqlBuilder.append(" SET ");
		List<String> propertyList = new ArrayList<>(Arrays.asList(propertyNames));
		// handle auto assign properties for update
		PropertyMapping updatedOnPropMapping = tableMapping.getUpdatedOnPropertyMapping();
		if (updatedOnPropMapping != null) {
			propertyList.add(updatedOnPropMapping.getPropertyName());
		}
		PropertyMapping updatedByPropMapping = tableMapping.getUpdatedByPropertyMapping();
		if (updatedByPropMapping != null) {
			propertyList.add(updatedByPropMapping.getPropertyName());
		}
		PropertyMapping versionPropMapping = tableMapping.getVersionPropertyMapping();
		if (versionPropMapping != null) {
			propertyList.add(versionPropMapping.getPropertyName());
		}
		boolean first = true;
		for (String propertyName : propertyList) {
			PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
			if (!first) {
				sqlBuilder.append(", ");
			} else {
				first = false;
			}
			sqlBuilder.append(propMapping.getColumnName());
			sqlBuilder.append(" = :");

			if (versionPropMapping != null
					&& propMapping.getPropertyName().equals(versionPropMapping.getPropertyName())) {
				sqlBuilder.append("incrementedVersion");
				params.add("incrementedVersion");
			} else {
				sqlBuilder.append(propMapping.getPropertyName());
				params.add(propMapping.getPropertyName());
			}
		}
		// the where clause
		sqlBuilder.append(" WHERE " + tableMapping.getIdColumnName() + " = :" + tableMapping.getIdPropertyName());
		params.add(tableMapping.getIdPropertyName());
		if (versionPropMapping != null) {
			sqlBuilder.append(" AND ").append(versionPropMapping.getColumnName()).append(" = :")
					.append(versionPropMapping.getPropertyName());
			params.add(versionPropMapping.getPropertyName());
		}
		String updateSql = sqlBuilder.toString();
		return new SqlAndParams(updateSql, params);
	}

	private <T> String getBeanColumnsSql(TableMapping tableMapping, Class<T> clazz) {
		String columnsSql = beanColumnsSqlCache.get(clazz.getName());
		if (columnsSql == null) {
			StringJoiner sj = new StringJoiner(", ", " ", " ");
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				String underscorePropertyName = SjmInternalUtils.toUnderscoreName(propMapping.getPropertyName());
				if (underscorePropertyName != null
						&& !underscorePropertyName.equalsIgnoreCase(propMapping.getColumnName())) {
					sj.add(propMapping.getColumnName() + " AS "
							+ SjmInternalUtils.toUnderscoreName(propMapping.getPropertyName()));
				} else {
					sj.add(propMapping.getColumnName());
				}
			}
			columnsSql = sj.toString();
			beanColumnsSqlCache.put(clazz.getName(), columnsSql);
		}
		return columnsSql;
	}

	private BeanWrapper getBeanWrapper(Object obj) {
		return PropertyAccessorFactory.forBeanPropertyAccess(obj);
	}

	// will return null when updateSpecificProperties property count is more than
	// CACHEABLE_UPDATE_PROPERTY_COUNT
	private String getUpdateSpecificPropertiesCacheKey(Object obj, String[] propertyNames) {
		if (propertyNames.length > CACHEABLE_UPDATE_PROPERTIES_COUNT) {
			return null;
		} else {
			return obj.getClass().getName() + "-" + String.join("-", propertyNames);
		}
	}

	SimpleCache<String, SimpleJdbcInsert> getInsertSqlCache() {
		return insertSqlCache;
	}

	SimpleCache<String, SqlAndParams> getUpdateSqlCache() {
		return updateSqlCache;
	}

	SimpleCache<String, SqlAndParams> getUpdateSpecificPropertiesSqlCache() {
		return updateSpecificPropertiesSqlCache;
	}

	SimpleCache<String, String> getBeanColumnsSqlCache() {
		return beanColumnsSqlCache;
	}

	private <T> BeanPropertyRowMapper<T> getBeanPropertyRowMapper(Class<T> clazz) {
		BeanPropertyRowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(clazz);
		rowMapper.setConversionService(this.conversionService);
		return rowMapper;
	}

	SimpleJdbcMapperSupport getSimpleJdbcMapperSupport() {
		return this.simpleJdbcMapperSupport;
	}

	public void setDatabaseMetaDataOverride(Map<Class<?>, Integer> databaseMetaDataOverrideMap) {
		simpleJdbcMapperSupport.setDatabaseMetaDataOverride(databaseMetaDataOverrideMap);
	}

}
