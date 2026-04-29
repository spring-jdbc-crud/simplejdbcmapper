package io.github.simplejdbcmapper.core;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.Assert;

import io.github.simplejdbcmapper.exception.MapperException;

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
		if (entities.size() < 2) {
			throw new MapperException("MultiEntity should have 2 or more entities configured.");
		}
		return entities;
	}

}
