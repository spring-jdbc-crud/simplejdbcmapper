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
package io.github.simplejdbcmapper.relationship;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

/**
 * This holds the results from a multi-entity query. See
 * {@link io.github.simplejdbcmapper.core.SimpleJdbcMapper#resultSetExtractor}
 * 
 * <p>
 * Starts the relationship processing flow.
 * 
 * For more details on its use see the <a href=
 * "https://github.com/spring-jdbc-crud/simplejdbcmapper#populating-relationships-from-custom-queries">documentation</a>
 *
 * 
 * @author Antony Joseph
 */
public class RelationshipMapper {

	private List<ExtractorEntityResult> results = new ArrayList<>();

	/**
	 * Add an entity result
	 * 
	 * @param <T>            the type
	 * @param entityType     the entity type
	 * @param list           the list of results
	 * @param idPropertyName the id property name of the entity
	 */
	public <T> void addEntityResult(Class<T> entityType, List<T> list, String idPropertyName) {
		Assert.notNull(entityType, "entityType must not be null");
		Assert.notNull(list, "list must not be null");

		results.add(new ExtractorEntityResult(entityType, list, idPropertyName));
	}

	/**
	 * Starts the relationship processing flow.
	 * 
	 * @param <T>  the type
	 * @param type the type
	 * @return RelationshipSpec the relationship spec
	 */
	public <T> RelationshipSpec type(Class<T> type) {
		Assert.notNull(type, "type must not be null");
		// will throw an exception for invalid type
		getExtractorEntityResult(type, results);
		return Relationship.newInstance(type, results);
	}

	/**
	 * returns the results for the type
	 * 
	 * @param <T>  the type
	 * @param type the type
	 * @return list of results
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<T> type) {
		ExtractorEntityResult result = getExtractorEntityResult(type, results);
		return (List<T>) result.list();
	}

	@SuppressWarnings("unchecked")
	static <T> List<T> getList(Class<?> type, List<ExtractorEntityResult> results) {
		ExtractorEntityResult result = getExtractorEntityResult(type, results);
		return (List<T>) result.list();
	}

	static ExtractorEntityResult getExtractorEntityResult(Class<?> type, List<ExtractorEntityResult> results) {
		for (ExtractorEntityResult result : results) {
			if (result.entityType() == type) {
				return result;
			}
		}
		throw new IllegalArgumentException(type + "was not part of the query result set");
	}

	record ExtractorEntityResult(Class<?> entityType, List<?> list, String idPropertyName) {
	}

}
