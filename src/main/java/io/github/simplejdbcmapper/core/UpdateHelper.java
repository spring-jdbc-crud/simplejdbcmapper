package io.github.simplejdbcmapper.core;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.support.SqlBinaryValue;
import org.springframework.jdbc.core.support.SqlCharacterValue;
import org.springframework.util.Assert;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.exception.OptimisticLockingException;

class UpdateHelper {
	private static final int CACHEABLE_UPDATE_PROPERTIES_COUNT = 3;

	private static final String INCREMENTED_VERSION = "incrementedVersion";

	private final SimpleJdbcMapperSupport sjms;
	private final TableMappingHelper tmh;

	// update sql cache
	// Map key - class name
	// value - the update sql and params
	private final SimpleCache<String, SqlAndParams> updateSqlCache = new SimpleCache<>();

	// update specified properties sql cache
	// Map key - class name and properties
	// value - the update sql and params
	private final SimpleCache<String, SqlAndParams> updateSpecificPropertiesSqlCache = new SimpleCache<>(2000);

	public UpdateHelper(SimpleJdbcMapperSupport sjms) {
		this.sjms = sjms;
		this.tmh = new TableMappingHelper(sjms);
	}

	public Integer update(Object obj) {
		Assert.notNull(obj, "Object must not be null");
		TableMapping tableMapping = tmh.getTableMapping(obj.getClass());
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

	public Integer updateSpecificProperties(Object obj, String... propertyNames) {
		Assert.notNull(obj, "Object must not be null");
		Assert.notNull(propertyNames, "propertyNames must not be null");
		TableMapping tableMapping = tmh.getTableMapping(obj.getClass());
		boolean foundInCache = false;
		SqlAndParams sqlAndParams = null;
		String cacheKey = getUpdateSpecificPropertiesCacheKey(obj, propertyNames);
		if (cacheKey != null) {
			sqlAndParams = updateSpecificPropertiesSqlCache.get(cacheKey);
		}
		if (sqlAndParams == null) {
			sqlAndParams = buildSqlAndParamsForUpdateSpecificProperties(tableMapping, propertyNames);
		} else {
			foundInCache = true;
		}
		Integer cnt = updateInternal(obj, sqlAndParams, tableMapping);
		if (cacheKey != null && !foundInCache && cnt > 0) {
			updateSpecificPropertiesSqlCache.put(cacheKey, sqlAndParams);
		}
		return cnt;
	}

	public SimpleCache<String, SqlAndParams> getUpdateSqlCache() {
		return updateSqlCache;
	}

	public SimpleCache<String, SqlAndParams> getUpdateSpecificPropertiesSqlCache() {
		return updateSpecificPropertiesSqlCache;
	}

	private Integer updateInternal(Object obj, SqlAndParams sqlAndParams, TableMapping tableMapping) {
		Assert.notNull(obj, "Object must not be null");
		Assert.notNull(sqlAndParams, "sqlAndParams must not be null");
		BeanWrapper bw = sjms.getBeanWrapper(obj);
		if (bw.getPropertyValue(tableMapping.getIdPropertyName()) == null) {
			throw new IllegalArgumentException("Property " + tableMapping.getTableClassName() + "."
					+ tableMapping.getIdPropertyName() + " is the id and must not be null.");
		}
		Set<String> parameters = sqlAndParams.getParams();
		populateAutoAssignProperties(tableMapping, bw, parameters);
		MapSqlParameterSource mapSqlParameterSource = createMapSqlParameterSource(tableMapping, bw, parameters);
		int cnt = -1;
		// if object has property version the version gets incremented on update.
		// throws OptimisticLockingException when update fails.
		if (sqlAndParams.getParams().contains(INCREMENTED_VERSION)) {
			cnt = sjms.getNamedParameterJdbcTemplate().update(sqlAndParams.getSql(), mapSqlParameterSource);
			if (cnt == 0) {
				throw new OptimisticLockingException(obj.getClass().getSimpleName()
						+ " update failed due to stale data. Failed for " + tableMapping.getIdColumnName() + " = "
						+ bw.getPropertyValue(tableMapping.getIdPropertyName()) + " and "
						+ tableMapping.getVersionPropertyMapping().getColumnName() + " = "
						+ bw.getPropertyValue(tableMapping.getVersionPropertyMapping().getPropertyName()));
			}
			// update the version in object with new version
			bw.setPropertyValue(tableMapping.getVersionPropertyMapping().getPropertyName(),
					mapSqlParameterSource.getValue(INCREMENTED_VERSION));
		} else {
			cnt = sjms.getNamedParameterJdbcTemplate().update(sqlAndParams.getSql(), mapSqlParameterSource);
		}
		return cnt;
	}

	private void populateAutoAssignProperties(TableMapping tableMapping, BeanWrapper bw, Set<String> parameters) {
		if (tableMapping.hasAutoAssignProperties()) {
			PropertyMapping updatedByPropMapping = tableMapping.getUpdatedByPropertyMapping();
			if (updatedByPropMapping != null && sjms.getRecordAuditedBySupplier() != null
					&& parameters.contains(updatedByPropMapping.getPropertyName())) {
				bw.setPropertyValue(updatedByPropMapping.getPropertyName(), sjms.getRecordAuditedBySupplier().get());
			}
			PropertyMapping updatedOnPropMapping = tableMapping.getUpdatedOnPropertyMapping();
			if (updatedOnPropMapping != null && sjms.getRecordAuditedOnSupplier() != null
					&& parameters.contains(updatedOnPropMapping.getPropertyName())) {
				bw.setPropertyValue(updatedOnPropMapping.getPropertyName(), sjms.getRecordAuditedOnSupplier().get());
			}
		}
	}

	private MapSqlParameterSource createMapSqlParameterSource(TableMapping tableMapping, BeanWrapper bw,
			Set<String> parameters) {
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		for (String paramName : parameters) {
			if (paramName.equals(INCREMENTED_VERSION)) {
				Integer incrementedVersionVal = getIncrementedVersionValue(tableMapping, bw);
				mapSqlParameterSource.addValue(INCREMENTED_VERSION, incrementedVersionVal, java.sql.Types.INTEGER);
			} else {
				int columnSqlType = tableMapping.getColumnOverriddenSqlType(paramName) == null
						? tableMapping.getColumnSqlType(paramName)
						: tableMapping.getColumnOverriddenSqlType(paramName);
				if (columnSqlType == Types.BLOB) {
					assignBlobMapSqlParameterSource(bw, mapSqlParameterSource, paramName);
				} else if (columnSqlType == Types.CLOB) {
					assignClobMapSqlParameterSource(bw, mapSqlParameterSource, paramName);
				} else {
					mapSqlParameterSource.addValue(paramName, bw.getPropertyValue(paramName), columnSqlType);
				}
			}
		}
		return mapSqlParameterSource;
	}

	private void assignBlobMapSqlParameterSource(BeanWrapper bw, MapSqlParameterSource mapSqlParameterSource,
			String paramName) {
		if (bw.getPropertyValue(paramName) == null) {
			mapSqlParameterSource.addValue(paramName, null, Types.BLOB);
		} else {
			mapSqlParameterSource.addValue(paramName, new SqlBinaryValue((byte[]) bw.getPropertyValue(paramName)),
					Types.BLOB);
		}
	}

	private void assignClobMapSqlParameterSource(BeanWrapper bw, MapSqlParameterSource mapSqlParameterSource,
			String paramName) {
		if (bw.getPropertyValue(paramName) == null) {
			mapSqlParameterSource.addValue(paramName, null, Types.CLOB);
		} else {
			mapSqlParameterSource.addValue(paramName, new SqlCharacterValue((char[]) bw.getPropertyValue(paramName)),
					Types.CLOB);
		}
	}

	private Integer getIncrementedVersionValue(TableMapping tableMapping, BeanWrapper bw) {
		Integer versionVal = (Integer) bw.getPropertyValue(tableMapping.getVersionPropertyMapping().getPropertyName());
		if (versionVal == null) {
			throw new MapperException(bw.getWrappedClass().getSimpleName() + "."
					+ tableMapping.getVersionPropertyMapping().getPropertyName()
					+ " is configured with annotation @Version. Property "
					+ tableMapping.getVersionPropertyMapping().getPropertyName() + " must not be null when updating.");
		}
		return versionVal + 1;
	}

	private List<String> getIgnoreAttributes(TableMapping tableMapping) {
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
		return ignoreAttrs;
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
		List<String> propertyList = tableMapping.getPropertyMappings().stream().map(pm -> pm.getPropertyName())
				.collect(Collectors.toList());
		List<String> ignoreAttrs = getIgnoreAttributes(tableMapping);
		propertyList.removeAll(ignoreAttrs);
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
		StringBuilder sqlBuilder = new StringBuilder("UPDATE ");
		sqlBuilder.append(tableMapping.fullyQualifiedTableName());
		sqlBuilder.append(" SET ");
		boolean first = true;
		PropertyMapping versionPropMapping = null;
		for (String propertyName : propertyList) {
			PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
			if (!first) {
				sqlBuilder.append(", ");
			} else {
				first = false;
			}
			sqlBuilder.append(propMapping.getColumnName());
			sqlBuilder.append(" = :");

			if (propMapping.isVersionAnnotation()) {
				sqlBuilder.append(INCREMENTED_VERSION);
				params.add(INCREMENTED_VERSION);
				versionPropMapping = propMapping;
			} else {
				sqlBuilder.append(propMapping.getPropertyName());
				params.add(propMapping.getPropertyName());
			}
		}
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

	private void validateUpdateSpecificProperties(TableMapping tableMapping, String... propertyNames) {
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

}
