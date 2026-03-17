package io.github.simplejdbcmapper.core;

import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

class DeleteOperation {
	private final SimpleJdbcMapperSupport sjmSupport;

	// Map key - class name
	// value - the sql
	private final SimpleCache<String, String> deleteSqlCache = new SimpleCache<>();

	public DeleteOperation(SimpleJdbcMapperSupport sjmSupport) {
		this.sjmSupport = sjmSupport;
	}

	public Integer delete(Object obj) {
		Assert.notNull(obj, "Object must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(obj.getClass());
		String sql = deleteSqlCache.get(obj.getClass().getName());
		if (sql == null) {
			sql = getDeleteSql(tableMapping);
			deleteSqlCache.put(obj.getClass().getName(), sql);
		}
		BeanWrapper bw = sjmSupport.getBeanWrapper(obj);
		Object id = bw.getPropertyValue(tableMapping.getIdPropertyName());
		return sjmSupport.getJdbcTemplate().update(sql, new SqlParameterValue(getIdSqlType(tableMapping), id));
	}

	public Integer deleteById(Class<?> clazz, Object id) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(id, "id must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
		String sql = deleteSqlCache.get(clazz.getName());
		if (sql == null) {
			sql = getDeleteSql(tableMapping);
			deleteSqlCache.put(clazz.getName(), sql);
		}
		return sjmSupport.getJdbcTemplate().update(sql, new SqlParameterValue(getIdSqlType(tableMapping), id));
	}

	SimpleCache<String, String> getDeleteSqlCache() {
		return deleteSqlCache;
	}

	private String getDeleteSql(TableMapping tableMapping) {
		return "DELETE FROM " + tableMapping.fullyQualifiedTableName() + " WHERE " + tableMapping.getIdColumnName()
				+ " = ?";
	}

	private int getIdSqlType(TableMapping tableMapping) {
		PropertyMapping idPropMapping = tableMapping.getIdPropertyMapping();
		return idPropMapping.getColumnOverriddenSqlType() == null ? idPropMapping.getColumnSqlType()
				: idPropMapping.getColumnOverriddenSqlType();
	}
}