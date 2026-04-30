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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.Assert;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * @author Antony Joseph
 */
public class MultiEntity {
	private final Map<Class<?>, String> entities = new LinkedHashMap<>();

	public MultiEntity add(Class<?> entityType, String tableAlias) {
		Assert.notNull(entityType, "entityType must not be null");
		InternalUtils.validateTableAlias(tableAlias);

		if (entities.containsValue(tableAlias)) {
			throw new IllegalArgumentException("duplicate tableAlias " + tableAlias);
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
