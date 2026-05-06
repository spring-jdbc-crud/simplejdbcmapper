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
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import io.github.simplejdbcmapper.relationship.RelationshipMapper.ExtractorEntityResult;

/**
 * The relationship. It uses the arguments provided through its API and
 * populates the target property.
 * 
 * <p>
 * Does not access database or use SimpleJdbcMapper
 * 
 * @author Antony Joseph
 */
public class Relationship implements RelationshipSpec, ToManySpec, ToOneSpec, PopulateSpec, GetListSpec {
	static final String IS_PREFIX = "is";
	static final String GET_PREFIX = "get";
	static final String SET_PREFIX = "set";

	private Class<?> mainType;
	private Class<?> relatedType;
	private List<ExtractorEntityResult> results = new ArrayList<>();

	private ToOne toOne;
	private ToMany toMany;

	private String relationshipType = "toOne";

	private Relationship(Class<?> mainType, List<ExtractorEntityResult> results) {
		this.mainType = mainType;
		this.results = results;

		this.toOne = new ToOne();
		this.toMany = new ToMany();
	}

	static RelationshipSpec newInstance(Class<?> type, List<ExtractorEntityResult> results) {
		return new Relationship(type, results);
	}

	public ToOneSpec toOne(Class<?> relatedType) {
		Assert.notNull(relatedType, "relatedType must not be null");
		// will throw an exception for invalid type
		getExtractorResult(relatedType);

		this.relatedType = relatedType;
		return this;
	}

	public ToManySpec toMany(Class<?> relatedType) {
		Assert.notNull(relatedType, "relatedType must not be null");
		// will throw an exception for invalid type
		getExtractorResult(relatedType);

		this.relatedType = relatedType;
		this.relationshipType = "toMany";
		return this;
	}

	public PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty) {
		if (relationshipType.equals("toOne")) {
			toOne.joinOn(mainObjJoinProperty, relatedObjJoinProperty, mainType, relatedType);
		} else if (relationshipType.equals("toMany")) {
			toMany.joinOn(mainObjJoinProperty, relatedObjJoinProperty, mainType, relatedType);
		}

		return this;
	}

	public PopulateSpec through(Class<?> throughType, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId) {
		Assert.notNull(throughType, "throughType must not be null");
		// will throw an exception for invalid type
		getExtractorResult(throughType);

		this.relationshipType = "toManyThrough";

		ExtractorEntityResult relatedResult = getExtractorResult(relatedType);
		ExtractorEntityResult mainResult = getExtractorResult(mainType);
		toMany.through(getList(throughType), fkPropertyToMainObjId, fkPropertyToRelatedObjId, mainType,
				mainResult.idPropertyName(), relatedType, relatedResult.idPropertyName(), throughType);

		return this;
	}

	public GetListSpec populate(String propertyToPopulateOnMainObj) {
		if (relationshipType.equals("toOne")) {
			toOne.populate(propertyToPopulateOnMainObj, mainType);
			toOne.populateToOne(getList(mainType), getList(relatedType));
		} else if (relationshipType.equals("toMany")) {
			toMany.populate(propertyToPopulateOnMainObj, mainType);
			toMany.populateToMany(getList(mainType), getList(relatedType));
		} else {
			// toManyThrough
			toMany.populate(propertyToPopulateOnMainObj, mainType);
			toMany.populateToManyThrough(getList(mainType), getList(relatedType));
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public <E> List<E> getList(Class<E> type) {
		for (ExtractorEntityResult result : results) {
			if (result.entityType() == type) {
				return (List<E>) result.list();
			}
		}
		throw new IllegalArgumentException(type + "was not part of the query result set");
	}

	private ExtractorEntityResult getExtractorResult(Class<?> type) {
		for (ExtractorEntityResult result : results) {
			if (result.entityType() == type) {
				return result;
			}
		}
		throw new IllegalArgumentException(type + "was not part of the query result set");
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

}
