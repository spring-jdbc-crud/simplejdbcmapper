package io.github.simplejdbcmapper.core;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.Assert;

public class MultiEntity {
	private final Map<Class<?>, String> entities = new LinkedHashMap<>();

	public MultiEntity add(Class<?> entityType, String tableAlias) {
		Assert.notNull(entityType, "entityType must not be null");
		InternalUtils.validateTableAlias(tableAlias);

		if (entities.containsValue(tableAlias)) {
			throw new IllegalArgumentException("duplicate tableAlias.");
		}

		entities.put(entityType, tableAlias);
		return this;
	}

	public Map<Class<?>, String> getEntities() {
		return entities;
	}

}
