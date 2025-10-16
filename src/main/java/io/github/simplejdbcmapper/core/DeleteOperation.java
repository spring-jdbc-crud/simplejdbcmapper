package io.github.simplejdbcmapper.core;

import org.springframework.beans.BeanWrapper;
import org.springframework.util.Assert;

class DeleteOperation {
	private final SimpleJdbcMapperSupport sjms;

	private final TableMappingHelper tmh;

	public DeleteOperation(SimpleJdbcMapperSupport sjms) {
		this.sjms = sjms;
		this.tmh = new TableMappingHelper(sjms);
	}

	public Integer delete(Object obj) {
		Assert.notNull(obj, "Object must not be null");
		TableMapping tableMapping = tmh.getTableMapping(obj.getClass());
		String sql = "DELETE FROM " + tableMapping.fullyQualifiedTableName() + " WHERE "
				+ tableMapping.getIdColumnName() + "= ?";
		BeanWrapper bw = sjms.getBeanWrapper(obj);
		Object id = bw.getPropertyValue(tableMapping.getIdPropertyName());
		return sjms.getJdbcTemplate().update(sql, id);
	}

	public Integer deleteById(Class<?> clazz, Object id) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(id, "id must not be null");
		TableMapping tableMapping = tmh.getTableMapping(clazz);
		String sql = "DELETE FROM " + tableMapping.fullyQualifiedTableName() + " WHERE "
				+ tableMapping.getIdColumnName() + " = ?";
		return sjms.getJdbcTemplate().update(sql, id);
	}
}