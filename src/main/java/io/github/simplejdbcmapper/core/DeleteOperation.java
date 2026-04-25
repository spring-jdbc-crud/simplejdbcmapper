/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.simplejdbcmapper.core;

import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

/**
 * Delete operations
 *
 * @author Antony Joseph
 */
class DeleteOperation {
	private final SimpleJdbcMapperSupport sjmSupport;

	private final SimpleCache<Class<?>, String> deleteSqlCache = new SimpleCache<>();

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

}