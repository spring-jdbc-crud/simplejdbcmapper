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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.Assert;

/**
 * This holds the results from a multi-entity query. See
 * {@link io.github.simplejdbcmapper.core.SimpleJdbcMapper#resultSetExtractor}
 * 
 * <p>
 * You can build relationships from multiple individual related lists. Note that
 * the lists have to be unique by id (ie no duplicates). For example if you have
 * a list of orders and another list of related orderLines you can do something
 * like:
 * 
 * <pre>
 * RelationhipMapper relationshipMapper = new RelationshipMapper();
 * relationshipMapper.addEntityResult(Order.class, orders, "id");
 * relationshipMapper.addEntityResult(OrderLine.class, orderLines, "id");
 * 
 * Relationship orderToManyOrderLine = 
 *      Relationshp.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId".populate("orderLines");
 *                                               
 * {@code List<Order>} orders = relationshipMapper.assemble(orderToManyOrderLine).getList(Order.class);
 * 
 * </pre>
 * 
 * For more details see <a href=
 * "https://github.com/spring-jdbc-crud/simplejdbcmapper#assembling-relationships-from-sql-queries">documentation</a>
 * and {@link io.github.simplejdbcmapper.relationship.Relationship}
 *
 * @author Antony Joseph
 */
public class RelationshipMapper implements GetListSpec {

	private List<ExtractorEntityResult> results = new ArrayList<>();

	/**
	 * Add an entity result
	 * 
	 * @param <T>            the type
	 * @param entityType     the entity type
	 * @param list           the list of results with no duplicates (unique by id)
	 * @param idPropertyName the id property name of the entity
	 */
	public <T> void addEntityResult(Class<T> entityType, List<T> list, String idPropertyName) {
		Assert.notNull(entityType, "entityType must not be null");
		Assert.notNull(list, "list must not be null");
		Assert.notNull(idPropertyName, "idPropertyName must not be null");
		checkDuplicatesForAddEntityResult(entityType);
		results.add(new ExtractorEntityResult(entityType, list, idPropertyName));
	}

	/**
	 * Assembles the relationships from the query results.
	 * 
	 * @param relationships an array of relationships
	 * @return GetListSpec
	 */
	public GetListSpec assemble(Relationship... relationships) {
		validateAssemble(relationships);
		for (Relationship rel : relationships) {
			process(rel);
		}
		return this;
	}

	/**
	 * Get the Id property name for type.
	 * 
	 * @param type The type
	 * @return String the id property name
	 */
	public String getIdPropertyName(Class<?> type) {
		ExtractorEntityResult result = getExtractorEntityResult(type);
		return result.idPropertyName();
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
		ExtractorEntityResult result = getExtractorEntityResult(type);
		return (List<T>) result.list();
	}

	void process(Relationship rel) {
		List<?> mainList = getList(rel.getMainType());
		List<?> relatedList = getList(rel.getRelatedType());
		if (rel.getRelationshipType().equals(RelationshipUtils.TO_ONE)) {
			rel.getToOne().process(mainList, relatedList);
		} else if (rel.getRelationshipType().equals(RelationshipUtils.TO_MANY)) {
			rel.getToMany().process(mainList, relatedList);
		} else {
			// toManyThrough
			List<?> throughList = getList(rel.getThroughType());
			rel.getToManyThrough().process(mainList, relatedList, throughList, getIdPropertyName(rel.getMainType()),
					getIdPropertyName(rel.getRelatedType()));
		}
	}

	private void checkDuplicatesForAddEntityResult(Class<?> entityType) {
		for (ExtractorEntityResult result : results) {
			if (result.entityType() == entityType) {
				throw new IllegalArgumentException("duplicate entityType " + entityType);
			}
		}
	}

	private void validateAssemble(Relationship... relationships) {
		if (relationships.length == 0) {
			throw new IllegalArgumentException("relationships array must not be empty.");
		}
		Set<String> set = new HashSet<>();
		for (Relationship rel : relationships) {
			if (rel == null) {
				throw new IllegalArgumentException("relationship must not be null in argument array. " + relationships);
			}
			if (!set.add(rel.getMainType().getName() + "-" + rel.getRelatedType().getName())) {
				throw new IllegalArgumentException("You cannot have multiple relationships between "
						+ rel.getMainType().getSimpleName() + " and  " + rel.getRelatedType().getSimpleName());
			}
		}
	}

	private ExtractorEntityResult getExtractorEntityResult(Class<?> type) {
		for (ExtractorEntityResult entityResult : results) {
			if (entityResult.entityType() == type) {
				return entityResult;
			}
		}
		throw new IllegalArgumentException(type + " was not part of the query results");
	}

	record ExtractorEntityResult(Class<?> entityType, List<?> list, String idPropertyName) {
	}

}
