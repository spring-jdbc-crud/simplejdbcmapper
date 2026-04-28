package io.github.simplejdbcmapper.core;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class EntityEntry {
	private final Class<?> entityType;
	private final String tableAlias;

	public EntityEntry(Class<?> entityType, String tableAlias) {
		Assert.notNull(entityType, "entityType must not be null");
		if (!StringUtils.hasText(tableAlias)) {
			throw new IllegalArgumentException("tableAlias has no value");
		}
		this.entityType = entityType;
		this.tableAlias = tableAlias;
	}

	public Class<?> getEntityType() {
		return entityType;
	}

	public String getTableAlias() {
		return tableAlias;
	}
}
