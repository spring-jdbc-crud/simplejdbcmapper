package io.github.simplejdbcmapper.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.github.simplejdbcmapper.exception.MapperException;

class FindOperation {
	private final SimpleJdbcMapperSupport sjmSupport;
	// Map key - class name
	// value - the sql
	private final SimpleCache<String, String> findByIdSqlCache = new SimpleCache<>();

	// Map key - classname
	// value - the column sql string
	private final SimpleCache<String, String> rawColumnsSqlCache = new SimpleCache<>();

	public FindOperation(SimpleJdbcMapperSupport sjmSupport) {
		this.sjmSupport = sjmSupport;
	}

	public <T> T findById(Class<T> entityType, Object id) {
		Assert.notNull(entityType, "entityType must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		String sql = findByIdSqlCache.get(entityType.getName());
		if (sql == null) {
			sql = "SELECT " + getRawSqlColumns(entityType) + " FROM " + tableMapping.fullyQualifiedTableName()
					+ " WHERE " + tableMapping.getIdColumnName() + " = ?";
			findByIdSqlCache.put(entityType.getName(), sql);
		}
		T obj = null;
		try {
			obj = sjmSupport.getJdbcTemplate().queryForObject(sql, getEntityRowMapper(entityType, tableMapping),
					new SqlParameterValue(tableMapping.getIdPropertyMapping().getEffectiveSqlType(), id));
		} catch (EmptyResultDataAccessException e) {
			// do nothing
		}
		return obj;
	}

	public <T> List<T> findAll(Class<T> entityType, SortBy... sortByArray) {
		Assert.notNull(entityType, "entityType must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		StringBuilder sql = new StringBuilder(256);
		sql.append("SELECT ").append(getRawSqlColumns(entityType)).append(" FROM ")
				.append(tableMapping.fullyQualifiedTableName())
				.append(orderByClause(entityType, sortByArray, tableMapping));
		return sjmSupport.getJdbcTemplate().query(sql.toString(), getEntityRowMapper(entityType, tableMapping));

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
		sql.append("SELECT ").append(getRawSqlColumns(entityType)).append(" FROM ")
				.append(tableMapping.fullyQualifiedTableName()).append(" WHERE ");
		if (propertyValue == null) {
			sql.append(propMapping.getColumnName()).append(" IS NULL");
		} else {
			sql.append(propMapping.getColumnName()).append(" = ?");
		}
		sql.append(orderByClause(entityType, sortByArray, tableMapping));
		if (propertyValue == null) {
			return sjmSupport.getJdbcTemplate().query(sql.toString(), getEntityRowMapper(entityType, tableMapping));
		} else {
			return sjmSupport.getJdbcTemplate().query(sql.toString(), getEntityRowMapper(entityType, tableMapping),
					new SqlParameterValue(propMapping.getEffectiveSqlType(), propertyValue));
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
		sql.append("SELECT ").append(getRawSqlColumns(entityType)).append(" FROM ")
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
			return sjmSupport.getJdbcTemplate().query(sql.toString(), getEntityRowMapper(entityType, tableMapping));
		} else {
			MapSqlParameterSource param = new MapSqlParameterSource();
			param.addValue("propertyValues", localPropertyValues, propMapping.getEffectiveSqlType());
			return sjmSupport.getNamedParameterJdbcTemplate().query(sql.toString(), param,
					getEntityRowMapper(entityType, tableMapping));
		}
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
		if (!StringUtils.hasText(tableAlias)) {
			throw new IllegalArgumentException("tableAlias has no value");
		}
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

	SimpleCache<String, String> getFindByIdSqlCache() {
		return findByIdSqlCache;
	}

	SimpleCache<String, String> getRawColumnsSqlCache() {
		return rawColumnsSqlCache;
	}

	String getRawSqlColumns(Class<?> entityType) {
		Assert.notNull(entityType, "entityType must not be null");
		String columnsSql = rawColumnsSqlCache.get(entityType.getName());
		if (columnsSql == null) {
			TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
			StringJoiner sj = new StringJoiner(", ", " ", " ");
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				sj.add(propMapping.getColumnName());
			}
			columnsSql = sj.toString();
			rawColumnsSqlCache.put(entityType.getName(), columnsSql);
		}
		return columnsSql;
	}

	private <T> EntityRowMapper<T> getEntityRowMapper(Class<T> type, TableMapping tableMapping) {
		return new EntityRowMapper<>(type, tableMapping, sjmSupport.getConversionService());
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
}
