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
import org.springframework.util.Assert;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.exception.OptimisticLockingException;

class UpdateOperation {
	private static final int CACHEABLE_UPDATE_SPECIFIC_PROPERTIES_COUNT = 5;

	private static final String INCREMENTED_VERSION = "[incrementedVersion]";

	private final SimpleJdbcMapperSupport sjmSupport;

	private SimpleCache<Class<?>, SqlAndParams> updateSqlCache = new SimpleCache<>();

	// Map key - class name and properties concatenated by hyphens
	// value - the update sql and params
	private SimpleCache<String, SqlAndParams> updateSpecificPropertiesSqlCache = new SimpleCache<>(2000);

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
		return updateInternal(object, sqlAndParams, tableMapping);
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
		return updateInternal(object, sqlAndParams, tableMapping);
	}

	SimpleCache<Class<?>, SqlAndParams> getUpdateSqlCache() {
		return updateSqlCache;
	}

	SimpleCache<String, SqlAndParams> getUpdateSpecificPropertiesSqlCache() {
		return updateSpecificPropertiesSqlCache;
	}

	// closing down simplejdbcmapper
	void close() {
		updateSqlCache = null;
		updateSpecificPropertiesSqlCache = null;
	}

	private Integer updateInternal(Object object, SqlAndParams sqlAndParams, TableMapping tableMapping) {
		Assert.notNull(object, "object must not be null");
		Assert.notNull(sqlAndParams, "sqlAndParams must not be null");
		BeanWrapper bw = sjmSupport.getBeanWrapper(object);
		if (bw.getPropertyValue(tableMapping.getIdPropertyName()) == null) {
			throw new IllegalArgumentException("Property " + tableMapping.getMappedObjClassName() + "."
					+ tableMapping.getIdPropertyName() + " is the id and must not be null.");
		}
		Set<String> parameters = sqlAndParams.getParams();
		populateAuditProperties(tableMapping, bw, parameters);
		MapSqlParameterSource mapSqlParameterSource = createMapSqlParameterSource(tableMapping, bw, parameters);
		int cnt = -1;
		// if object has property version the version gets incremented on update.
		// throws OptimisticLockingException when update fails.
		if (sqlAndParams.getParams().contains(INCREMENTED_VERSION)) {
			cnt = sjmSupport.getNamedParameterJdbcTemplate().update(sqlAndParams.getSql(), mapSqlParameterSource);
			if (cnt == 0) {
				throw new OptimisticLockingException(object.getClass().getSimpleName()
						+ " update failed due to stale data. Failed for " + tableMapping.getIdColumnName() + " = "
						+ bw.getPropertyValue(tableMapping.getIdPropertyName()) + " and "
						+ tableMapping.getVersionPropertyMapping().getColumnName() + " = "
						+ bw.getPropertyValue(tableMapping.getVersionPropertyMapping().getPropertyName()));
			}
			// update the version in object with new version
			bw.setPropertyValue(tableMapping.getVersionPropertyMapping().getPropertyName(),
					mapSqlParameterSource.getValue(INCREMENTED_VERSION));
		} else {
			cnt = sjmSupport.getNamedParameterJdbcTemplate().update(sqlAndParams.getSql(), mapSqlParameterSource);
		}
		return cnt;
	}

	private void populateAuditProperties(TableMapping tableMapping, BeanWrapper bw, Set<String> parameters) {
		if (tableMapping.hasAutoAssignProperties()) {
			PropertyMapping updatedByPropMapping = tableMapping.getUpdatedByPropertyMapping();
			if (updatedByPropMapping != null && sjmSupport.getRecordAuditedBySupplier() != null
					&& parameters.contains(updatedByPropMapping.getPropertyName())) {
				bw.setPropertyValue(updatedByPropMapping.getPropertyName(),
						sjmSupport.getRecordAuditedBySupplier().get());
			}
			PropertyMapping updatedOnPropMapping = tableMapping.getUpdatedOnPropertyMapping();
			if (updatedOnPropMapping != null && sjmSupport.getRecordAuditedOnSupplier() != null
					&& parameters.contains(updatedOnPropMapping.getPropertyName())) {
				bw.setPropertyValue(updatedOnPropMapping.getPropertyName(),
						sjmSupport.getRecordAuditedOnSupplier().get());
			}
		}
	}

	private MapSqlParameterSource createMapSqlParameterSource(TableMapping tableMapping, BeanWrapper bw,
			Set<String> parameters) {
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		for (String paramName : parameters) {
			if (paramName.equals(INCREMENTED_VERSION)) {
				Integer incrementedVersionVal = getIncrementedVersionValue(tableMapping, bw);
				mapSqlParameterSource.addValue(INCREMENTED_VERSION, incrementedVersionVal, Types.INTEGER);
			} else {
				PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(paramName);
				Integer columnSqlType = propMapping.getEffectiveSqlType();
				if (propMapping.isBinaryLargeObject()) {
					InternalUtils.assignBlobMapSqlParameterSource(bw, mapSqlParameterSource, propMapping, columnSqlType,
							false);
				} else if (propMapping.isCharacterLargeObject()) {
					InternalUtils.assignClobMapSqlParameterSource(bw, mapSqlParameterSource, propMapping, columnSqlType,
							false);
				} else if (propMapping.isEnum()) {
					InternalUtils.assignEnumMapSqlParameterSource(bw, mapSqlParameterSource, propMapping, columnSqlType,
							false);
				} else {
					mapSqlParameterSource.addValue(paramName, bw.getPropertyValue(paramName), columnSqlType);
				}
			}
		}
		return mapSqlParameterSource;
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
						+ tableMapping.getMappedObjClassName());
			}
			if (propertyMapping.isIdAnnotation()) {
				throw new MapperException("Id property " + tableMapping.getMappedObjClassName() + "." + propertyName
						+ " cannot be updated using updateSpecificProperties() method.");
			}
			if (propertyMapping.isCreatedByAnnotation() || propertyMapping.isCreatedOnAnnotation()
					|| propertyMapping.isUpdatedByAnnotation() || propertyMapping.isUpdatedOnAnnotation()
					|| propertyMapping.isVersionAnnotation()) {
				throw new MapperException("Auto assign property " + tableMapping.getMappedObjClassName() + "."
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
