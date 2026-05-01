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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * Used in multi entity processing. Information in the class is used to generate
 * the multi-entity sql columns and for the frameworks resultSetExtractorr
 * 
 * @author Antony Joseph
 */
public class MultiEntity {
	private final List<Map.Entry<Class<?>, String>> entries = new ArrayList<>();

	/**
	 * Used add entities for multi-entity processing.
	 * 
	 * @param entityType The entityType
	 * @param tableAlias The table alias for the entity which is used when
	 *                   generating the columns sql
	 * @return the MultiEntity
	 */
	public MultiEntity add(Class<?> entityType, String tableAlias) {
		Assert.notNull(entityType, "entityType must not be null");
		InternalUtils.validateTableAlias(tableAlias);
		checkDuplicateAlias(tableAlias);
		entries.add(Map.entry(entityType, tableAlias));
		return this;
	}

	List<Map.Entry<Class<?>, String>> getEntries() {
		if (entries.size() < 2) {
			throw new MapperException("MultiEntity should have 2 or more entities configured.");
		}
		return entries;
	}

	private void checkDuplicateAlias(String tableAlias) {
		for (Map.Entry<Class<?>, String> entry : entries) {
			if (entry.getValue().equals(tableAlias)) {
				throw new IllegalArgumentException("duplicate tableAlias " + tableAlias);
			}
		}
	}

}
