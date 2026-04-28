package io.github.simplejdbcmapper.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultiEntity {
	private final Map<Class<?>, String> entities = new LinkedHashMap<>();

	public MultiEntity add(Class<?> entityType, String tableAlias) {
		entities.put(entityType, tableAlias);
		return this;
	}

	public Map<Class<?>, String> getEntities() {
		return entities;
	}

}
