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

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.Assert;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.exception.OptimisticLockingException;

/**
 * Update operations
 *
 * @author Antony Joseph
 */
class UpdateOperation {
	private static final int CACHEABLE_UPDATE_SPECIFIC_PROPERTIES_COUNT = 5;

	private static final String INCREMENTED_VERSION = "[incrementedVersion]";

	private final SimpleJdbcMapperSupport sjmSupport;

	private final SimpleCache<Class<?>, SqlAndParams> updateSqlCache = new SimpleCache<>();

	// Map key - class name and properties concatenated by hyphens
	// value - the update sql and params
	private final SimpleCache<String, SqlAndParams> updateSpecificPropertiesSqlCache = new SimpleCache<>(3000);

	public UpdateOperation(SimpleJdbcMapperSupport sjmSupport) {
		this.sjmSupport = sjmSupport;
	}

	public Integer update(Object object) {
		Assert.notNull(object, "object must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(object.getClass());
		SqlAndParams sqlAndParams = updateSqlCache.get(object.getClass());
		if (sqlAndParams == null) {
			sqlAndParams = buildSqlAndParamsForUpdate(tableMapping);
			updateSqlCache.put(object.getClass(), sqlAndParams);
		}
		EntityWrapper ew = new EntityWrapper(object, tableMapping);
		return updateInternal(ew, sqlAndParams);
	}

	public Integer updateSpecificProperties(Object object, String... propertyNames) {
		Assert.notNull(object, "object must not be null");
		Assert.notNull(propertyNames, "propertyNames must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(object.getClass());
		SqlAndParams sqlAndParams = null;
		String cacheKey = getUpdateSpecificPropertiesCacheKey(object, propertyNames);
		if (cacheKey != null) {
			sqlAndParams = updateSpecificPropertiesSqlCache.get(cacheKey);
		}
		if (sqlAndParams == null) {
			sqlAndParams = buildSqlAndParamsForUpdateSpecificProperties(tableMapping, propertyNames);
			if (cacheKey != null) {
				updateSpecificPropertiesSqlCache.put(cacheKey, sqlAndParams);
			}
		}
		EntityWrapper ew = new EntityWrapper(object, tableMapping);
		return updateInternal(ew, sqlAndParams);
	}

	SimpleCache<Class<?>, SqlAndParams> getUpdateSqlCache() {
		return updateSqlCache;
	}

	SimpleCache<String, SqlAndParams> getUpdateSpecificPropertiesSqlCache() {
		return updateSpecificPropertiesSqlCache;
	}

	private Integer updateInternal(EntityWrapper ew, SqlAndParams sqlAndParams) {
		TableMapping tableMapping = ew.getTableMapping();
		Assert.notNull(sqlAndParams, "sqlAndParams must not be null");
		if (ew.getPropertyValue(tableMapping.getIdPropertyMapping()) == null) {
			throw new IllegalArgumentException("Property " + tableMapping.getMappedObjType().getName() + "."
					+ tableMapping.getIdPropertyName() + " is the id and must not be null.");
		}
		Set<String> parameters = sqlAndParams.getParams();
		populateAuditProperties(ew);
		MapSqlParameterSource mapSqlParameterSource = createMapSqlParameterSource(ew, parameters);
		int cnt = -1;
		// if object has property version the version gets incremented on update.
		// throws OptimisticLockingException when update fails.
		if (sqlAndParams.getParams().contains(INCREMENTED_VERSION)) {
			cnt = sjmSupport.getNamedParameterJdbcTemplate().update(sqlAndParams.getSql(), mapSqlParameterSource);
			if (cnt == 0) {
				throw new OptimisticLockingException(ew.getWrappedClass().getSimpleName()
						+ " update failed due to stale data. Failed for " + tableMapping.getIdColumnName() + " = "
						+ ew.getPropertyValue(tableMapping.getIdPropertyMapping()) + " and "
						+ tableMapping.getVersionPropertyMapping().getColumnName() + " = "
						+ ew.getPropertyValue(tableMapping.getVersionPropertyMapping()));
			}
			// update the version in object with new version
			ew.setPropertyValue(tableMapping.getVersionPropertyMapping(),
					mapSqlParameterSource.getValue(INCREMENTED_VERSION));
		} else {
			cnt = sjmSupport.getNamedParameterJdbcTemplate().update(sqlAndParams.getSql(), mapSqlParameterSource);
		}
		return cnt;
	}

	private void populateAuditProperties(EntityWrapper ew) {
		TableMapping tableMapping = ew.getTableMapping();
		if (tableMapping.hasAutoAssignProperties()) {
			PropertyMapping updatedByPropMapping = tableMapping.getUpdatedByPropertyMapping();
			if (updatedByPropMapping != null && sjmSupport.getRecordAuditedBySupplier() != null) {
				ew.setPropertyValue(updatedByPropMapping, sjmSupport.getRecordAuditedBySupplier().get());
			}
			PropertyMapping updatedOnPropMapping = tableMapping.getUpdatedOnPropertyMapping();
			if (updatedOnPropMapping != null && sjmSupport.getRecordAuditedOnSupplier() != null) {
				ew.setPropertyValue(updatedOnPropMapping, sjmSupport.getRecordAuditedOnSupplier().get());
			}
		}
	}

	private MapSqlParameterSource createMapSqlParameterSource(EntityWrapper ew, Set<String> parameters) {
		TableMapping tableMapping = ew.getTableMapping();
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		for (String propertyName : parameters) {
			if (propertyName.equals(INCREMENTED_VERSION)) {
				Integer incrementedVersionVal = getIncrementedVersionValue(ew);
				mapSqlParameterSource.addValue(INCREMENTED_VERSION, incrementedVersionVal, Types.INTEGER);
			} else {
				PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
				Object val = ew.getPropertyValue(propMapping);
				Integer columnSqlType = propMapping.getColumnSqlType();
				if (propMapping.isBinaryLargeObject()) {
					InternalUtils.assignBlobMapSqlParameterSource(mapSqlParameterSource, val, propertyName,
							columnSqlType);
				} else if (propMapping.isCharacterLargeObject()) {
					InternalUtils.assignClobMapSqlParameterSource(mapSqlParameterSource, val, propertyName,
							columnSqlType);
				} else if (propMapping.isEnum()) {
					if (val == null) {
						mapSqlParameterSource.addValue(propertyName, null, columnSqlType);
					} else {
						mapSqlParameterSource.addValue(propertyName, ((Enum<?>) val).name(), columnSqlType);
					}
				} else {
					mapSqlParameterSource.addValue(propertyName, val, columnSqlType);
				}
			}
		}
		return mapSqlParameterSource;
	}

	private Integer getIncrementedVersionValue(EntityWrapper ew) {
		TableMapping tableMapping = ew.getTableMapping();
		Integer versionVal = (Integer) ew.getPropertyValue(tableMapping.getVersionPropertyMapping());
		if (versionVal == null) {
			throw new MapperException(ew.getWrappedClass().getSimpleName() + "."
					+ tableMapping.getVersionPropertyMapping().getPropertyName()
					+ " is configured with annotation @Version. Property "
					+ tableMapping.getVersionPropertyMapping().getPropertyName() + " must not be null when updating.");
		}
		return versionVal + 1;
	}

	private List<String> getIgnoreProperties(TableMapping tableMapping) {
		List<String> ignoreProps = new ArrayList<>();
		ignoreProps.add(tableMapping.getIdPropertyName());
		PropertyMapping createdOnPropMapping = tableMapping.getCreatedOnPropertyMapping();
		if (createdOnPropMapping != null) {
			ignoreProps.add(createdOnPropMapping.getPropertyName());
		}
		PropertyMapping createdByPropMapping = tableMapping.getCreatedByPropertyMapping();
		if (createdByPropMapping != null) {
			ignoreProps.add(createdByPropMapping.getPropertyName());
		}
		return ignoreProps;
	}

	private List<String> getAutoAssignProperties(TableMapping tableMapping) {
		List<String> list = new ArrayList<>();
		PropertyMapping updatedOnPropMapping = tableMapping.getUpdatedOnPropertyMapping();
		if (updatedOnPropMapping != null) {
			list.add(updatedOnPropMapping.getPropertyName());
		}
		PropertyMapping updatedByPropMapping = tableMapping.getUpdatedByPropertyMapping();
		if (updatedByPropMapping != null) {
			list.add(updatedByPropMapping.getPropertyName());
		}
		PropertyMapping versionPropMapping = tableMapping.getVersionPropertyMapping();
		if (versionPropMapping != null) {
			list.add(versionPropMapping.getPropertyName());
		}
		return list;
	}

	private SqlAndParams buildSqlAndParamsForUpdate(TableMapping tableMapping) {
		Assert.notNull(tableMapping, "tableMapping must not be null");
		List<String> propertyList = Arrays.stream(tableMapping.getPropertyMappings()).map(pm -> pm.getPropertyName())
				.collect(Collectors.toList());
		List<String> ignoreProps = getIgnoreProperties(tableMapping);
		propertyList.removeAll(ignoreProps);
		return buildSqlAndParams(tableMapping, propertyList);
	}

	private SqlAndParams buildSqlAndParamsForUpdateSpecificProperties(TableMapping tableMapping,
			String... propertyNames) {
		Assert.notNull(tableMapping, "tableMapping must not be null");
		Assert.notNull(propertyNames, "propertyNames must not be null");
		validateUpdateSpecificProperties(tableMapping, propertyNames);
		List<String> propertyList = new ArrayList<>(Arrays.asList(propertyNames));
		propertyList.addAll(getAutoAssignProperties(tableMapping));
		return buildSqlAndParams(tableMapping, propertyList);
	}

	private SqlAndParams buildSqlAndParams(TableMapping tableMapping, List<String> propertyList) {
		Assert.notNull(tableMapping, "tableMapping must not be null");
		Assert.notNull(propertyList, "propertyList must not be null");
		Set<String> params = new HashSet<>();
		StringBuilder sql = new StringBuilder(256);
		sql.append("UPDATE ").append(tableMapping.fullyQualifiedTableName()).append(" SET ");
		boolean first = true;
		PropertyMapping versionPropMapping = null;
		for (String propertyName : propertyList) {
			PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
			if (!first) {
				sql.append(", ");
			} else {
				first = false;
			}
			sql.append(propMapping.getColumnName());
			sql.append(" = :");

			if (propMapping.isVersionAnnotation()) {
				sql.append(INCREMENTED_VERSION);
				params.add(INCREMENTED_VERSION);
				versionPropMapping = propMapping;
			} else {
				sql.append(propMapping.getPropertyName());
				params.add(propMapping.getPropertyName());
			}
		}
		sql.append(" WHERE ").append(tableMapping.getIdColumnName()).append(" = :")
				.append(tableMapping.getIdPropertyName());
		params.add(tableMapping.getIdPropertyName());
		if (versionPropMapping != null) {
			sql.append(" AND ").append(versionPropMapping.getColumnName()).append(" = :")
					.append(versionPropMapping.getPropertyName());
			params.add(versionPropMapping.getPropertyName());
		}
		return new SqlAndParams(sql.toString(), params);
	}

	private void validateUpdateSpecificProperties(TableMapping tableMapping, String... propertyNames) {
		for (String propertyName : propertyNames) {
			PropertyMapping propertyMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
			if (propertyMapping == null) {
				throw new MapperException("No mapping found for property '" + propertyName + "' in class "
						+ tableMapping.getMappedObjType().getName());
			}
			if (propertyMapping.isIdAnnotation()) {
				throw new MapperException("Id property " + tableMapping.getMappedObjType().getName() + "."
						+ propertyName + " cannot be updated using updateSpecificProperties() method.");
			}
			if (propertyMapping.isCreatedByAnnotation() || propertyMapping.isCreatedOnAnnotation()
					|| propertyMapping.isUpdatedByAnnotation() || propertyMapping.isUpdatedOnAnnotation()
					|| propertyMapping.isVersionAnnotation()) {
				throw new MapperException("Auto assign property " + tableMapping.getMappedObjType().getName() + "."
						+ propertyName + " cannot be updated using updateSpecificProperties() method.");
			}
		}
	}

	private String getUpdateSpecificPropertiesCacheKey(Object object, String[] propertyNames) {
		if (propertyNames.length > CACHEABLE_UPDATE_SPECIFIC_PROPERTIES_COUNT) {
			return null;
		} else {
			return object.getClass().getName() + "-" + String.join("-", propertyNames);
		}
	}

}
