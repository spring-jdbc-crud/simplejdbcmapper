package io.github.simplejdbcmapper.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

class FindOperation {
	private final SimpleJdbcMapperSupport sjmSupport;

	// Map key - class name
	// value - the sql
	private final SimpleCache<String, String> findByIdSqlCache = new SimpleCache<>();

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
		boolean foundInCache = false;
		String sql = findByIdSqlCache.get(clazz.getName());
		if (sql == null) {
			sql = "SELECT " + getBeanFriendlySqlColumns(clazz) + " FROM " + tableMapping.fullyQualifiedTableName()
					+ " WHERE " + tableMapping.getIdColumnName() + " = ?";
		} else {
			foundInCache = true;
		}
		BeanPropertyRowMapper<T> rowMapper = getBeanPropertyRowMapper(clazz);
		T obj = null;
		try {
			obj = sjmSupport.getJdbcTemplate().queryForObject(sql, rowMapper,
					new SqlParameterValue(tableMapping.getIdColumnSqlType(), id));
		} catch (EmptyResultDataAccessException e) {
			// do nothing
		}
		if (!foundInCache && obj != null) {
			findByIdSqlCache.put(clazz.getName(), sql);
		}
		return obj;
	}

	public <T> List<T> findAll(Class<T> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
		String sql = "SELECT " + getBeanFriendlySqlColumns(clazz) + " FROM " + tableMapping.fullyQualifiedTableName();
		BeanPropertyRowMapper<T> rowMapper = getBeanPropertyRowMapper(clazz);
		return sjmSupport.getJdbcTemplate().query(sql, rowMapper);
	}

	public String getBeanFriendlySqlColumns(Class<?> clazz) {
		String columnsSql = beanColumnsSqlCache.get(clazz.getName());
		if (columnsSql == null) {
			TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
			StringJoiner sj = new StringJoiner(", ", " ", " ");
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				String underscorePropertyName = InternalUtils.toUnderscoreName(propMapping.getPropertyName());
				if (!underscorePropertyName.equalsIgnoreCase(propMapping.getColumnName())) {
					sj.add(propMapping.getColumnName() + " AS " + underscorePropertyName);
				} else {
					sj.add(propMapping.getColumnName());
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

	SimpleCache<String, String> getBeanColumnsSqlCache() {
		return beanColumnsSqlCache;
	}

	private <T> BeanPropertyRowMapper<T> getBeanPropertyRowMapper(Class<T> clazz) {
		BeanPropertyRowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(clazz);
		rowMapper.setConversionService(sjmSupport.getConversionService());
		return rowMapper;
	}

}
