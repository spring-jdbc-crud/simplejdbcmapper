package io.github.simplejdbcmapper.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

class FindHelper {
	private final SimpleJdbcMapperSupport sjms;

	private final TableMappingHelper tmh;

	// Map key - class name
	// value - the sql
	private final SimpleCache<String, String> findByIdSqlCache = new SimpleCache<>();

	// the column sql string with bean friendly column aliases for mapped properties
	// of model.
	// Map key - class name
	// value - the column sql string
	private final SimpleCache<String, String> beanColumnsSqlCache = new SimpleCache<>();

	public FindHelper(SimpleJdbcMapperSupport sjms) {
		this.sjms = sjms;
		this.tmh = new TableMappingHelper(sjms);
	}

	public <T> T findById(Class<T> clazz, Object id) {
		Assert.notNull(clazz, "Class must not be null");
		TableMapping tableMapping = tmh.getTableMapping(clazz);
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
			obj = sjms.getJdbcTemplate().queryForObject(sql, rowMapper,
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
		TableMapping tableMapping = tmh.getTableMapping(clazz);
		String sql = "SELECT " + getBeanColumnsSql(tableMapping, clazz) + " FROM "
				+ tableMapping.fullyQualifiedTableName();
		BeanPropertyRowMapper<T> rowMapper = getBeanPropertyRowMapper(clazz);
		return sjms.getJdbcTemplate().query(sql, rowMapper);
	}

	public String getBeanFriendlySqlColumns(Class<?> clazz) {
		return getBeanColumnsSql(tmh.getTableMapping(clazz), clazz);
	}

	public Map<String, String> getPropertyToColumnMappings(Class<?> clazz) {
		TableMapping tableMapping = tmh.getTableMapping(clazz);
		Map<String, String> map = new LinkedHashMap<>();
		for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
			map.put(propMapping.getPropertyName(), propMapping.getColumnName());
		}
		return map;
	}

	public SimpleCache<String, String> getFindByIdSqlCache() {
		return findByIdSqlCache;
	}

	public SimpleCache<String, String> getBeanColumnsSqlCache() {
		return beanColumnsSqlCache;
	}

	public TableMapping getTableMapping(Class<?> clazz) {
		return tmh.getTableMapping(clazz);
	}

	public SimpleCache<String, TableMapping> getTableMappingCache() {
		return tmh.getTableMappingCache();
	}

	private <T> String getBeanColumnsSql(TableMapping tableMapping, Class<T> clazz) {
		String columnsSql = beanColumnsSqlCache.get(clazz.getName());
		if (columnsSql == null) {
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

	private <T> BeanPropertyRowMapper<T> getBeanPropertyRowMapper(Class<T> clazz) {
		BeanPropertyRowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(clazz);
		rowMapper.setConversionService(sjms.getConversionService());
		return rowMapper;
	}

}
