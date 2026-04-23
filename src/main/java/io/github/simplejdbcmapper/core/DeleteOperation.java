package io.github.simplejdbcmapper.core;

import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

class DeleteOperation {
	private final SimpleJdbcMapperSupport sjmSupport;

	private SimpleCache<Class<?>, String> deleteSqlCache = new SimpleCache<>();

	public DeleteOperation(SimpleJdbcMapperSupport sjmSupport) {
		this.sjmSupport = sjmSupport;
	}

	public Integer delete(Object object) {
		Assert.notNull(object, "object must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(object.getClass());
		BeanWrapper bw = sjmSupport.getBeanWrapper(object);
		return deleteById(object.getClass(), bw.getPropertyValue(tableMapping.getIdPropertyName()));
	}

	public Integer deleteById(Class<?> entityType, Object id) {
		Assert.notNull(entityType, "entityType must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		String sql = deleteSqlCache.get(entityType);
		if (sql == null) {
			sql = "DELETE FROM " + tableMapping.fullyQualifiedTableName() + " WHERE " + tableMapping.getIdColumnName()
					+ " = ?";
			deleteSqlCache.put(entityType, sql);
		}
		return sjmSupport.getJdbcTemplate().update(sql,
				new SqlParameterValue(tableMapping.getIdPropertyMapping().getEffectiveSqlType(), id));
	}

	SimpleCache<Class<?>, String> getDeleteSqlCache() {
		return deleteSqlCache;
	}

	// closing down simplejdbcmapper
	void close() {
		deleteSqlCache = null;
	}

}