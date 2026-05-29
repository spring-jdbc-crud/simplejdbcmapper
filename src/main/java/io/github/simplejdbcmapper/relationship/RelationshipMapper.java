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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * This holds the results from a multi-entity query. See
 * {@link io.github.simplejdbcmapper.core.SimpleJdbcMapper#resultSetExtractor}
 * 
 * <p>
 * You can build relationships from multiple individual related lists. For
 * example if you have a list of orders and another list of related orderLines
 * you can do something like:
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
 * "https://github.com/spring-jdbc-crud/simplejdbcmapper#assembling-relationships-from-custom-queries">documentation</a>
 * and {@link io.github.simplejdbcmapper.relationship.Relationship}
 *
 * @author Antony Joseph
 */
public class RelationshipMapper implements GetListSpec {
	static final String IS_PREFIX = "is";
	static final String GET_PREFIX = "get";
	static final String SET_PREFIX = "set";

	static final String TO_MANY = "toMany";
	static final String TO_ONE = "toOne";
	static final String TO_MANY_THROUGH = "toManyThrough";

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
		Assert.notNull(idPropertyName, "idPropertyName must not be null");
		checkDuplicatesForAddEntityResult(entityType);
		results.add(new ExtractorEntityResult(entityType, list, idPropertyName));
	}

	/**
	 * @deprecated As of release 2.4.0, Replaced by
	 *             {@link io.github.simplejdbcmapper.relationship.Relationship#type}
	 *             Starts the relationship processing flow.
	 * 
	 * @param <T>  the type
	 * @param type the type
	 * @return RelationshipSpec the relationship spec
	 */
	@Deprecated(since = "2.4.0", forRemoval = true)
	public <T> RelationshipSpec type(Class<T> type) {
		Assert.notNull(type, "type must not be null");
		// will throw an exception for invalid type
		getList(type);
		return RelationshipLegacy.newInstance(type, results);
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

	void process(Relationship rel) {
		List<?> mainList = getList(rel.getMainType());
		List<?> relatedList = getList(rel.getRelatedType());
		if (rel.getRelationshipType().equals(TO_ONE)) {
			rel.getToOne().process(mainList, relatedList);
		} else if (rel.getRelationshipType().equals(TO_MANY)) {
			rel.getToMany().process(mainList, relatedList);
		} else {
			// toManyThrough
			List<?> throughList = getList(rel.getThroughType());
			rel.getToManyThrough().process(mainList, relatedList, throughList, getIdPropertyName(rel.getMainType()),
					getIdPropertyName(rel.getRelatedType()));
		}
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

	String getIdPropertyName(Class<?> type) {
		ExtractorEntityResult result = getExtractorEntityResult(type, results);
		return result.idPropertyName();
	}

	private void checkDuplicatesForAddEntityResult(Class<?> entityType) {
		for (ExtractorEntityResult result : results) {
			if (result.entityType() == entityType) {
				throw new IllegalArgumentException("duplicate entityType " + entityType);
			}
		}
	}

	private void validateAssemble(Relationship... relationships) {
		Assert.notNull(relationships, "relationships must not be null");
		if (relationships.length == 0) {
			throw new IllegalArgumentException("relationships array must not be empty.");
		}
		Set<String> set = new HashSet<>();
		for (Relationship rel : relationships) {
			if (rel == null) {
				throw new IllegalArgumentException("relationship must not be null");
			}
			if (!set.add(rel.getMainType().getName() + "-" + rel.getRelatedType().getName())) {
				throw new IllegalArgumentException("Duplicate relationship. " + rel);
			}
		}
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
		throw new IllegalArgumentException(type + " was not part of the query results");
	}

	static Method getReadMethod(Class<?> type, String propertyName) {
		Method m = ReflectionUtils.findMethod(type, GET_PREFIX + StringUtils.capitalize(propertyName));
		if (m == null) {
			m = ReflectionUtils.findMethod(type, IS_PREFIX + StringUtils.capitalize(propertyName));
		}
		if (m == null) {
			throw new IllegalArgumentException(
					"Invalid argument. Could not find getter for " + type.getName() + "." + propertyName);
		}
		// turn off jvm access verification for invoke()
		m.trySetAccessible();
		return m;
	}

	static Method getWriteMethod(Class<?> type, String propertyName) {
		Field field = ReflectionUtils.findField(type, propertyName);
		if (field == null) {
			throw new IllegalArgumentException(
					"Invalid argument. Property name " + propertyName + " does not exist for " + type.getName());
		} else {
			Method m = ReflectionUtils.findMethod(type, SET_PREFIX + StringUtils.capitalize(propertyName),
					field.getType());
			if (m == null) {
				throw new IllegalArgumentException(
						"Invalid argument. Could not find setter for " + type.getName() + "." + propertyName);
			}
			// turn off jvm access verification for invoke()
			m.trySetAccessible();
			return m;
		}
	}

	static Class<?> getPropertyType(Class<?> type, String propertyName) {
		Field field = ReflectionUtils.findField(type, propertyName);
		if (field != null) {
			return field.getType();
		} else {
			throw new IllegalArgumentException(
					"Invalid argument. Property name " + propertyName + " does not exist for " + type.getName());
		}
	}

	record ExtractorEntityResult(Class<?> entityType, List<?> list, String idPropertyName) {
	}

}
