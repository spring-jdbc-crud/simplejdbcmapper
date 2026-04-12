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
		BeanWrapper bw = sjmSupport.getBeanWrapper(obj);
		return deleteById(obj.getClass(), bw.getPropertyValue(tableMapping.getIdPropertyName()));
	}

	public Integer deleteById(Class<?> clazz, Object id) {
		Assert.notNull(clazz, "Class must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
		String sql = deleteSqlCache.get(clazz.getName());
		if (sql == null) {
			sql = "DELETE FROM " + tableMapping.fullyQualifiedTableName() + " WHERE " + tableMapping.getIdColumnName()
					+ " = ?";
			deleteSqlCache.put(clazz.getName(), sql);
		}
		return sjmSupport.getJdbcTemplate().update(sql,
				new SqlParameterValue(tableMapping.getIdPropertyMapping().getEffectiveSqlType(), id));
	}

	SimpleCache<String, String> getDeleteSqlCache() {
		return deleteSqlCache;
	}

}