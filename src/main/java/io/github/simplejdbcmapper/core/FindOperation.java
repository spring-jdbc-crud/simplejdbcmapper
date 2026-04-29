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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * The find operations
 *
 * @author Antony Joseph
 */
class FindOperation {
	private final SimpleJdbcMapperSupport sjmSupport;

	private final SimpleCache<Class<?>, String> findByIdSqlCache = new SimpleCache<>();

	private final SimpleCache<Class<?>, String> entitySqlColumnsCache = new SimpleCache<>();

	// Map key - classname-tableAlias
	// value - the column sql string
	private final SimpleCache<String, String> entitySqlColumnsAliasCache = new SimpleCache<>(5000);

	public FindOperation(SimpleJdbcMapperSupport sjmSupport) {
		this.sjmSupport = sjmSupport;
	}

	public <T> T findById(Class<T> entityType, Object id) {
		Assert.notNull(entityType, "entityType must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		String sql = findByIdSqlCache.get(entityType);
		if (sql == null) {
			sql = "SELECT " + getEntitySqlColumns(entityType) + " FROM " + tableMapping.fullyQualifiedTableName()
					+ " WHERE " + tableMapping.getIdColumnName() + " = ?";
			findByIdSqlCache.put(entityType, sql);
		}
		T obj = null;
		try {
			obj = sjmSupport.getJdbcTemplate().queryForObject(sql, newEntityRowMapper(entityType),
					new SqlParameterValue(tableMapping.getIdPropertyMapping().getColumnSqlType(), getValue(id)));
		} catch (EmptyResultDataAccessException e) {
			// do nothing
		}
		return obj;
	}

	public <T> List<T> findAll(Class<T> entityType, SortBy... sortByArray) {
		Assert.notNull(entityType, "entityType must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		StringBuilder sql = new StringBuilder(256);
		sql.append("SELECT ").append(getEntitySqlColumns(entityType)).append(" FROM ")
				.append(tableMapping.fullyQualifiedTableName())
				.append(orderByClause(entityType, sortByArray, tableMapping));
		return sjmSupport.getJdbcTemplate().query(sql.toString(), newEntityRowMapper(entityType));
	}

	public <T> List<T> findByPropertyValue(Class<T> entityType, String propertyName, Object propertyValue,
			SortBy... sortByArray) {
		Assert.notNull(entityType, "entityType must not be null");
		Assert.notNull(propertyName, "propertyName must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
		if (propMapping == null) {
			throw new MapperException(entityType.getSimpleName() + "." + propertyName + " does not have a mapping.");
		}
		StringBuilder sql = new StringBuilder(256);
		sql.append("SELECT ").append(getEntitySqlColumns(entityType)).append(" FROM ")
				.append(tableMapping.fullyQualifiedTableName()).append(" WHERE ");
		if (propertyValue == null) {
			sql.append(propMapping.getColumnName()).append(" IS NULL");
		} else {
			sql.append(propMapping.getColumnName()).append(" = ?");
		}
		sql.append(orderByClause(entityType, sortByArray, tableMapping));
		if (propertyValue == null) {
			return sjmSupport.getJdbcTemplate().query(sql.toString(), newEntityRowMapper(entityType));
		} else {
			return sjmSupport.getJdbcTemplate().query(sql.toString(), newEntityRowMapper(entityType),
					new SqlParameterValue(propMapping.getColumnSqlType(), getValue(propertyValue)));
		}
	}

	public <T, U> List<T> findByPropertyValues(Class<T> entityType, String propertyName, Collection<U> propertyValues,
			SortBy... sortByArray) {
		Assert.notNull(entityType, "entityType must not be null");
		Assert.notNull(propertyName, "propertyName must not be null");
		Assert.notNull(propertyValues, "propertyValues must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
		if (propMapping == null) {
			throw new MapperException(entityType.getSimpleName() + "." + propertyName + " does not have a mapping.");
		}
		if (ObjectUtils.isEmpty(propertyValues)) {
			return new ArrayList<>();
		}
		Set<U> localPropertyValues = new LinkedHashSet<>(propertyValues);
		boolean hasNullInSet = localPropertyValues.remove(null); // need to handle nulls in the set.
		StringBuilder sql = new StringBuilder(256);
		sql.append("SELECT ").append(getEntitySqlColumns(entityType)).append(" FROM ")
				.append(tableMapping.fullyQualifiedTableName()).append(" WHERE ");
		if (ObjectUtils.isEmpty(localPropertyValues)) {
			sql.append(propMapping.getColumnName()).append(" IS NULL");
		} else {
			sql.append(propMapping.getColumnName()).append(" IN (:propertyValues)");
			if (hasNullInSet) {
				sql.append(" OR ").append(propMapping.getColumnName()).append(" IS NULL");
			}
		}
		sql.append(orderByClause(entityType, sortByArray, tableMapping));
		if (ObjectUtils.isEmpty(localPropertyValues)) {
			return sjmSupport.getJdbcTemplate().query(sql.toString(), newEntityRowMapper(entityType));
		} else {
			Set<?> values = getValues(localPropertyValues);
			MapSqlParameterSource param = new MapSqlParameterSource();
			param.addValue("propertyValues", values, propMapping.getColumnSqlType());
			return sjmSupport.getNamedParameterJdbcTemplate().query(sql.toString(), param,
					newEntityRowMapper(entityType));
		}
	}

	public String getEntitySqlColumns(Class<?> entityType) {
		Assert.notNull(entityType, "entityType must not be null");
		String columnsSql = entitySqlColumnsCache.get(entityType);
		if (columnsSql == null) {
			TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
			StringJoiner sj = new StringJoiner(", ", " ", " ");
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				sj.add(propMapping.getColumnName());
			}
			columnsSql = sj.toString();
			entitySqlColumnsCache.put(entityType, columnsSql);
		}
		return columnsSql;
	}

	public String getEntitySqlColumns(Class<?> entityType, String tableAlias) {
		Assert.notNull(entityType, "entityType must not be null");
		InternalUtils.validateTableAlias(tableAlias);
		String cacheKey = entityType.getName() + "-" + tableAlias;
		String columnsSql = entitySqlColumnsAliasCache.get(cacheKey);
		if (columnsSql == null) {
			String tablePrefix = tableAlias + ".";
			TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
			StringJoiner sj = new StringJoiner(", ", " ", " ");
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				sj.add(tablePrefix + propMapping.getColumnName());
			}
			columnsSql = sj.toString();
			entitySqlColumnsAliasCache.put(cacheKey, columnsSql);
		}
		return columnsSql;
	}

	public <T> EntityRowMapper<T> newEntityRowMapper(Class<T> entityType) {
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		return new EntityRowMapper<>(tableMapping, sjmSupport.getConversionService(), 1);
	}

	public String getBeanFriendlySqlColumns(Class<?> entityType) {
		Assert.notNull(entityType, "entityType must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		StringJoiner sj = new StringJoiner(", ", " ", " ");
		for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
			String underscorePropertyName = InternalUtils.toUnderscoreName(propMapping.getPropertyName());
			if (underscorePropertyName.equals(propMapping.getColumnName())) {
				sj.add(propMapping.getColumnName());
			} else {
				sj.add(propMapping.getColumnName() + " AS " + underscorePropertyName);
			}
		}
		return sj.toString();
	}

	public String getBeanFriendlySqlColumns(Class<?> entityType, String tableAlias) {
		Assert.notNull(entityType, "entityType must not be null");
		InternalUtils.validateTableAlias(tableAlias);
		String tablePrefix = tableAlias + ".";
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		StringJoiner sj = new StringJoiner(", ", " ", " ");
		for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
			String underscorePropertyName = InternalUtils.toUnderscoreName(propMapping.getPropertyName());
			if (underscorePropertyName.equals(propMapping.getColumnName())) {
				sj.add(tablePrefix + propMapping.getColumnName());
			} else {
				sj.add(tablePrefix + propMapping.getColumnName() + " AS " + underscorePropertyName);
			}
		}
		return sj.toString();
	}

	public Map<String, String> getPropertyToColumnMappings(Class<?> entityType) {
		Assert.notNull(entityType, "entityType must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		Map<String, String> map = new LinkedHashMap<>();
		for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
			map.put(propMapping.getPropertyName(), propMapping.getColumnName());
		}
		return map;
	}

	public String getMultiEntitySqlColumns(MultiEntity multiEntity) {
		StringJoiner sj = new StringJoiner(", ", " ", " ");
		for (Map.Entry<Class<?>, String> entry : multiEntity.getEntities().entrySet()) {
			String tablePrefix = entry.getValue() + ".";
			String colPrefix = entry.getValue() + "_";
			TableMapping tableMapping = sjmSupport.getTableMapping(entry.getKey());
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				sj.add(tablePrefix + propMapping.getColumnName() + " AS " + colPrefix + propMapping.getColumnName());
			}
		}
		return sj.toString();
	}

	@SuppressWarnings("rawtypes")
	public ResultSetExtractor<Map<Class, List>> resultSetExtractor(MultiEntity multiEntity) {
		int offset = 1;
		Map<Class, List> tempResultMap = new LinkedHashMap<>();
		Map<Class, EntityRowMapper> mapRowMappers = new LinkedHashMap<>();
		for (Map.Entry<Class<?>, String> entry : multiEntity.getEntities().entrySet()) {
			tempResultMap.put(entry.getKey(), new ArrayList());
			TableMapping tableMapping = sjmSupport.getTableMapping(entry.getKey());
			mapRowMappers.put(entry.getKey(),
					new EntityRowMapper(tableMapping, sjmSupport.getConversionService(), offset));
			offset += tableMapping.getPropertyMappings().length;
		}

		return new ResultSetExtractor<Map<Class, List>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Map<Class, List> extractData(ResultSet rs) throws SQLException, DataAccessException {
				int rowCnt = 1;
				while (rs.next()) {
					for (Map.Entry<Class, EntityRowMapper> entry : mapRowMappers.entrySet()) {
						EntityRowMapper rowMapper = entry.getValue();
						Object obj = rowMapper.mapRow(rs, rowCnt);
						if (obj != null) {
							tempResultMap.get(entry.getKey()).add(obj);
						}
					}
					rowCnt++;
				}
				Map<Class, List> resultMap = new HashMap<>();
				for (Map.Entry<Class, List> entry : tempResultMap.entrySet()) {
					resultMap.put(entry.getKey(), distinctById(entry.getKey(), entry.getValue()));
				}
				return resultMap;
			}
		};
	}

	SimpleCache<Class<?>, String> getFindByIdSqlCache() {
		return findByIdSqlCache;
	}

	SimpleCache<Class<?>, String> getEntitySqlColumnsCache() {
		return entitySqlColumnsCache;
	}

	SimpleCache<String, String> getEntitySqlColumnsAliasCache() {
		return entitySqlColumnsAliasCache;
	}

	private String orderByClause(Class<?> entityType, SortBy[] sortByArray, TableMapping tableMapping) {
		if (sortByArray.length > 0) {
			StringBuilder clause = new StringBuilder(64);
			clause.append(" ORDER BY ");
			int cnt = 0;
			for (SortBy sortBy : sortByArray) {
				PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(sortBy.getPropertyName());
				if (propMapping == null) {
					throw new IllegalArgumentException(
							sortBy.getPropertyName() + " is not a mapped property for class " + entityType.getName());
				}
				if (cnt > 0) {
					clause.append(", ");
				}
				clause.append(propMapping.getColumnName()).append(" ").append(sortBy.getDirection());
				cnt++;
			}
			return clause.toString();
		} else {
			return "";
		}
	}

	private Object getValue(Object obj) {
		if (obj != null && obj.getClass().isEnum()) {
			return ((Enum<?>) obj).name();
		}
		return obj;
	}

	private Set<?> getValues(Set<?> set) {
		Object obj = set.iterator().next();
		if (obj != null && obj.getClass().isEnum()) {
			Set<String> values = new LinkedHashSet<>();
			for (Object val : set) {
				values.add(((Enum<?>) val).name());
			}
			return values;
		}
		return set;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List distinctById(Class entityType, List list) {
		List resultList = new ArrayList();
		Set set = new HashSet();
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		PropertyMapping idPropMapping = tableMapping.getIdPropertyMapping();
		for (Object entityObj : list) {
			try {
				Object id = idPropMapping.getReadMethod().invoke(entityObj);
				if (id != null && set.add(id)) {
					resultList.add(entityObj);
				}
			} catch (Exception e) {
				throw new MapperException(e);
			}
		}
		return resultList;
	}

}
