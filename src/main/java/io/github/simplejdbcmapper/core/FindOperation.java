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
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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

	// the column sql string with bean friendly column aliases for mapped properties
	// Map key - class name
	// value - the column sql string
	private final SimpleCache<String, String> beanColumnsSqlCache = new SimpleCache<>();

	// the column sql string with bean friendly column aliases for mapped properties
	// Map key - classname-tableAlias
	// value - the column sql string
	private final SimpleCache<String, String> beanColumnsTableAliasSqlCache = new SimpleCache<>(2000);

	public FindOperation(SimpleJdbcMapperSupport sjmSupport) {
		this.sjmSupport = sjmSupport;
	}

	public <T> T findById(Class<T> type, Object id) {
		Assert.notNull(type, "type must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(type);
		String sql = findByIdSqlCache.get(type.getName());
		if (sql == null) {
			sql = "SELECT " + getBeanFriendlySqlColumns(type) + " FROM " + tableMapping.fullyQualifiedTableName()
					+ " WHERE " + tableMapping.getIdColumnName() + " = ?";
			findByIdSqlCache.put(type.getName(), sql);
		}
		T obj = null;
		try {
			obj = sjmSupport.getJdbcTemplate().queryForObject(sql, getBeanPropertyRowMapper(type),
					new SqlParameterValue(tableMapping.getIdPropertyMapping().getEffectiveSqlType(), id));
		} catch (EmptyResultDataAccessException e) {
			// do nothing
		}
		return obj;
	}

	public <T> List<T> findAll(Class<T> type, SortBy... sortByArray) {
		Assert.notNull(type, "type must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(type);
		StringBuilder sql = new StringBuilder(256);
		sql.append("SELECT ").append(getBeanFriendlySqlColumns(type)).append(" FROM ")
				.append(tableMapping.fullyQualifiedTableName()).append(orderByClause(type, sortByArray, tableMapping));
		return sjmSupport.getJdbcTemplate().query(sql.toString(), getBeanPropertyRowMapper(type));
	}

	public <T> List<T> findByPropertyValue(Class<T> type, String propertyName, Object propertyValue,
			SortBy... sortByArray) {
		Assert.notNull(type, "type must not be null");
		Assert.notNull(propertyName, "propertyName must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(type);
		PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
		if (propMapping == null) {
			throw new MapperException(type.getSimpleName() + "." + propertyName + " does not have a mapping.");
		}
		StringBuilder sql = new StringBuilder(256);
		sql.append("SELECT ").append(getBeanFriendlySqlColumns(type)).append(" FROM ")
				.append(tableMapping.fullyQualifiedTableName()).append(" WHERE ");
		if (propertyValue == null) {
			sql.append(propMapping.getColumnName()).append(" IS NULL");
		} else {
			sql.append(propMapping.getColumnName()).append(" = ?");
		}
		sql.append(orderByClause(type, sortByArray, tableMapping));
		if (propertyValue == null) {
			return sjmSupport.getJdbcTemplate().query(sql.toString(), getBeanPropertyRowMapper(type));
		} else {
			return sjmSupport.getJdbcTemplate().query(sql.toString(), getBeanPropertyRowMapper(type),
					new SqlParameterValue(propMapping.getEffectiveSqlType(), propertyValue));
		}
	}

	public <T, U> List<T> findByPropertyValues(Class<T> type, String propertyName, Collection<U> propertyValues,
			SortBy... sortByArray) {
		Assert.notNull(type, "type must not be null");
		Assert.notNull(propertyName, "propertyName must not be null");
		Assert.notNull(propertyValues, "propertyValues must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(type);
		PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
		if (propMapping == null) {
			throw new MapperException(type.getSimpleName() + "." + propertyName + " does not have a mapping.");
		}
		if (ObjectUtils.isEmpty(propertyValues)) {
			return new ArrayList<>();
		}
		Set<U> localPropertyValues = new LinkedHashSet<>(propertyValues);
		boolean hasNullInSet = localPropertyValues.remove(null); // need to handle nulls in the set.
		StringBuilder sql = new StringBuilder(256);
		sql.append("SELECT ").append(getBeanFriendlySqlColumns(type)).append(" FROM ")
				.append(tableMapping.fullyQualifiedTableName()).append(" WHERE ");
		if (ObjectUtils.isEmpty(localPropertyValues)) {
			sql.append(propMapping.getColumnName()).append(" IS NULL");
		} else {
			sql.append(propMapping.getColumnName()).append(" IN (:propertyValues)");
			if (hasNullInSet) {
				sql.append(" OR ").append(propMapping.getColumnName()).append(" IS NULL");
			}
		}
		sql.append(orderByClause(type, sortByArray, tableMapping));
		if (ObjectUtils.isEmpty(localPropertyValues)) {
			return sjmSupport.getJdbcTemplate().query(sql.toString(), getBeanPropertyRowMapper(type));
		} else {
			MapSqlParameterSource param = new MapSqlParameterSource();
			param.addValue("propertyValues", localPropertyValues, propMapping.getEffectiveSqlType());
			return sjmSupport.getNamedParameterJdbcTemplate().query(sql.toString(), param,
					getBeanPropertyRowMapper(type));
		}

	}

	public String getBeanFriendlySqlColumns(Class<?> type) {
		Assert.notNull(type, "type must not be null");
		String columnsSql = beanColumnsSqlCache.get(type.getName());
		if (columnsSql == null) {
			TableMapping tableMapping = sjmSupport.getTableMapping(type);
			StringJoiner sj = new StringJoiner(", ", " ", " ");
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				String underscorePropertyName = InternalUtils.toUnderscoreName(propMapping.getPropertyName());
				if (underscorePropertyName.equals(propMapping.getColumnName())) {
					sj.add(propMapping.getColumnName());
				} else {
					sj.add(propMapping.getColumnName() + " AS " + underscorePropertyName);
				}
			}
			columnsSql = sj.toString();
			beanColumnsSqlCache.put(type.getName(), columnsSql);
		}
		return columnsSql;
	}

	public String getBeanFriendlySqlColumns(Class<?> type, String tableAlias) {
		Assert.notNull(type, "type must not be null");
		if (!StringUtils.hasText(tableAlias)) {
			throw new IllegalArgumentException("tableAlias has no value");
		}
		String cacheKey = type.getName() + "-" + tableAlias;
		String columnsSql = beanColumnsTableAliasSqlCache.get(cacheKey);
		if (columnsSql == null) {
			String tablePrefix = tableAlias + ".";
			TableMapping tableMapping = sjmSupport.getTableMapping(type);
			StringJoiner sj = new StringJoiner(", ", " ", " ");
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				String underscorePropertyName = InternalUtils.toUnderscoreName(propMapping.getPropertyName());
				if (underscorePropertyName.equals(propMapping.getColumnName())) {
					sj.add(tablePrefix + propMapping.getColumnName());
				} else {
					sj.add(tablePrefix + propMapping.getColumnName() + " AS " + underscorePropertyName);
				}
			}
			columnsSql = sj.toString();
			beanColumnsTableAliasSqlCache.put(cacheKey, columnsSql);
		}
		return columnsSql;
	}

	public Map<String, String> getPropertyToColumnMappings(Class<?> type) {
		Assert.notNull(type, "type must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(type);
		Map<String, String> map = new LinkedHashMap<>();
		for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
			map.put(propMapping.getPropertyName(), propMapping.getColumnName());
		}
		return map;
	}

	SimpleCache<String, String> getFindByIdSqlCache() {
		return findByIdSqlCache;
	}

	SimpleCache<String, String> getBeanColumnsSqlCache() {
		return beanColumnsSqlCache;
	}

	SimpleCache<String, String> getBeanColumnsTableAliasSqlCache() {
		return beanColumnsTableAliasSqlCache;
	}

	private <T> BeanPropertyRowMapper<T> getBeanPropertyRowMapper(Class<T> type) {
		BeanPropertyRowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(type);
		rowMapper.setConversionService(sjmSupport.getConversionService());
		return rowMapper;
	}

	private String orderByClause(Class<?> clazz, SortBy[] sortByArray, TableMapping tableMapping) {
		if (sortByArray.length > 0) {
			StringBuilder clause = new StringBuilder(64);
			clause.append(" ORDER BY ");
			int cnt = 0;
			for (SortBy sortBy : sortByArray) {
				PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(sortBy.getPropertyName());
				if (propMapping == null) {
					throw new IllegalArgumentException(
							sortBy.getPropertyName() + " is not a mapped property for class " + clazz.getName());
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
