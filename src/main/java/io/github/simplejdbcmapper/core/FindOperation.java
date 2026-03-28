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

import io.github.simplejdbcmapper.exception.MapperException;

class FindOperation {
	private final SimpleJdbcMapperSupport sjmSupport;

	// Map key - class name
	// value - the sql
	private final SimpleCache<String, String> findByIdSqlCache = new SimpleCache<>();

	// Map key - class name
	// value - the sql
	private final SimpleCache<String, String> findAllSqlCache = new SimpleCache<>();

	// the column sql string with bean friendly column aliases for mapped properties
	// Map key - class name
	// value - the column sql string
	private final SimpleCache<String, String> beanColumnsSqlCache = new SimpleCache<>();

	public FindOperation(SimpleJdbcMapperSupport sjmSupport) {
		this.sjmSupport = sjmSupport;
	}

	public <T> T findById(Class<T> clazz, Object id) {
		Assert.notNull(clazz, "Class must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
		String sql = findByIdSqlCache.get(clazz.getName());
		if (sql == null) {
			sql = "SELECT " + getBeanFriendlySqlColumns(clazz) + " FROM " + tableMapping.fullyQualifiedTableName()
					+ " WHERE " + tableMapping.getIdColumnName() + " = ?";
			findByIdSqlCache.put(clazz.getName(), sql);
		}
		T obj = null;
		try {
			obj = sjmSupport.getJdbcTemplate().queryForObject(sql, getBeanPropertyRowMapper(clazz),
					new SqlParameterValue(tableMapping.getIdPropertyMapping().getEffectiveSqlType(), id));
		} catch (EmptyResultDataAccessException e) {
			// do nothing
		}
		return obj;
	}

	public <T> List<T> findAll(Class<T> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
		String sql = findAllSqlCache.get(clazz.getName());
		if (sql == null) {
			sql = "SELECT " + getBeanFriendlySqlColumns(clazz) + " FROM " + tableMapping.fullyQualifiedTableName();
			findAllSqlCache.put(clazz.getName(), sql);
		}
		return sjmSupport.getJdbcTemplate().query(sql, getBeanPropertyRowMapper(clazz));
	}

	public <T> List<T> findByPropertyValue(Class<T> clazz, String propertyName, Object propertyValue) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(propertyName, "propertyName must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
		PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
		if (propMapping == null) {
			throw new MapperException(clazz.getSimpleName() + "." + propertyName + " does not have a mapping.");
		}
		String sql = "SELECT " + getBeanFriendlySqlColumns(clazz) + " FROM " + tableMapping.fullyQualifiedTableName()
				+ " WHERE ";
		if (propertyValue == null) {
			sql += propMapping.getColumnName() + " IS NULL";
			return sjmSupport.getJdbcTemplate().query(sql, getBeanPropertyRowMapper(clazz));
		} else {
			sql += propMapping.getColumnName() + " = ?";
			return sjmSupport.getJdbcTemplate().query(sql, getBeanPropertyRowMapper(clazz),
					new SqlParameterValue(propMapping.getEffectiveSqlType(), propertyValue));
		}
	}

	public <T, U extends Object> List<T> findByPropertyValues(Class<T> clazz, String propertyName,
			Collection<U> propertyValues) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(propertyName, "propertyName must not be null");
		Assert.notNull(propertyValues, "propertyValues must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
		PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
		if (propMapping == null) {
			throw new MapperException(clazz.getSimpleName() + "." + propertyName + " does not have a mapping.");
		}
		if (ObjectUtils.isEmpty(propertyValues)) {
			return new ArrayList<>();
		}
		Set<U> localPropertyValues = new LinkedHashSet<>(propertyValues);
		boolean hasNullInSet = localPropertyValues.remove(null); // need to handle nulls in the set.
		String sql = "SELECT " + getBeanFriendlySqlColumns(clazz) + " FROM " + tableMapping.fullyQualifiedTableName()
				+ " WHERE ";
		if (ObjectUtils.isEmpty(localPropertyValues)) {
			sql += propMapping.getColumnName() + " IS NULL";
			return sjmSupport.getNamedParameterJdbcTemplate().query(sql, getBeanPropertyRowMapper(clazz));
		} else {
			sql += propMapping.getColumnName() + " IN (:propertyValues)";
			if (hasNullInSet) {
				sql += " OR " + propMapping.getColumnName() + " IS NULL";
			}
			MapSqlParameterSource param = new MapSqlParameterSource();
			param.addValue("propertyValues", localPropertyValues, propMapping.getEffectiveSqlType());
			return sjmSupport.getNamedParameterJdbcTemplate().query(sql, param, getBeanPropertyRowMapper(clazz));
		}
	}

	public String getBeanFriendlySqlColumns(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		String columnsSql = beanColumnsSqlCache.get(clazz.getName());
		if (columnsSql == null) {
			TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
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
			beanColumnsSqlCache.put(clazz.getName(), columnsSql);
		}
		return columnsSql;
	}

	public Map<String, String> getPropertyToColumnMappings(Class<?> clazz) {
		TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
		Map<String, String> map = new LinkedHashMap<>();
		for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
			map.put(propMapping.getPropertyName(), propMapping.getColumnName());
		}
		return map;
	}

	SimpleCache<String, String> getFindByIdSqlCache() {
		return findByIdSqlCache;
	}

	SimpleCache<String, String> getFindAllSqlCache() {
		return findAllSqlCache;
	}

	SimpleCache<String, String> getBeanColumnsSqlCache() {
		return beanColumnsSqlCache;
	}

	private <T> BeanPropertyRowMapper<T> getBeanPropertyRowMapper(Class<T> clazz) {
		BeanPropertyRowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(clazz);
		rowMapper.setConversionService(sjmSupport.getConversionService());
		return rowMapper;
	}
}
