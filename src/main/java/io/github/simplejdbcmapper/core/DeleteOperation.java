package io.github.simplejdbcmapper.core;

import org.springframework.beans.BeanWrapper;
import org.springframework.util.Assert;

class DeleteOperation {
	private final SimpleJdbcMapperSupport sjmSupport;

	public DeleteOperation(SimpleJdbcMapperSupport sjmSupport) {
		this.sjmSupport = sjmSupport;
	}

	public Integer delete(Object obj) {
		Assert.notNull(obj, "Object must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(obj.getClass());
		String sql = "DELETE FROM " + tableMapping.fullyQualifiedTableName() + " WHERE "
				+ tableMapping.getIdColumnName() + "= ?";
		BeanWrapper bw = sjmSupport.getBeanWrapper(obj);
		Object id = bw.getPropertyValue(tableMapping.getIdPropertyName());
		return sjmSupport.getJdbcTemplate().update(sql, id);
	}

	public Integer deleteById(Class<?> clazz, Object id) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(id, "id must not be null");
		TableMapping tableMapping = sjmSupport.getTableMapping(clazz);
		String sql = "DELETE FROM " + tableMapping.fullyQualifiedTableName() + " WHERE "
				+ tableMapping.getIdColumnName() + " = ?";
		return sjmSupport.getJdbcTemplate().update(sql, id);
	}
}