package io.github.simplejdbcmapper.relationship;

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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * The starting point for any relationship assignment. It works with the lists
 * provided and depending on the arguments to the api methods, populates the
 * target property. The terminal method in the api flow is populate() which
 * triggers the processing.
 * 
 * <p>
 * Processing does not access the database or use SimpleJdbcMapper.
 * 
 * @author Antony Joseph
 */
public class RelationshipMapper implements RelationshipSpec, ToManySpec, ToOneSpec, PopulateSpec, GetListSpec {
	static final String IS_PREFIX = "is";
	static final String GET_PREFIX = "get";
	static final String SET_PREFIX = "set";

	// private final Map<Class<?>, List<?>> map = new HashMap<>();

	private List<ExtractorResult> results = new ArrayList<>();

	private ToOne toOne;

	private ToMany toMany;

	private Class<?> mainType;
	private Class<?> relatedType;

	private String relationshipType = "toOne";

	public <T> void addList(Class<T> type, List<T> list) {
		results.add(new ExtractorResult(type, list, null));
	}

	public <T> void addResult(Class<T> type, List<T> list, String idPropertyName) {
		results.add(new ExtractorResult(type, list, idPropertyName));
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<T> type) {
		for (ExtractorResult result : results) {
			if (result.entityType == type) {
				return (List<T>) result.list;
			}
		}
		return null;
	}

	ExtractorResult getExtractorResult(Class<?> type) {
		for (ExtractorResult result : results) {
			if (result.entityType == type) {
				return result;
			}
		}
		return null;
	}

	// private List<T> mainObjList;

	public RelationshipMapper() {
		this.toOne = new ToOne();
		this.toMany = new ToMany();
	}

	// private RelationshipMapper(List<?> mainObjList) {
	// this.mainObjList = mainObjList;
	// }

	/**
	 * Start of creating a relationship. The main object is the object whose
	 * property will be populated using the api.
	 * 
	 * @param <T>         the main object type
	 * @param mainObjList the main object list
	 * @return A relationship
	 */
	public <T> RelationshipSpec type(Class<T> mainType) {
		this.mainType = mainType;
		return this;
	}

	/**
	 * A toOne relationship
	 * 
	 * @param <U>            the type of the related object
	 * 
	 * @param relatedObjList the list of related objects
	 * 
	 */
	public <U> ToOneSpec toOne(Class<U> relatedType) {
		this.relatedType = relatedType;
		return (ToOneSpec) this;
	}

	public <U> ToManySpec toMany(Class<U> relatedType) {
		this.relatedType = relatedType;
		this.relationshipType = "toMany";
		return (ToManySpec) this;
	}

	public PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty) {
		if (relationshipType.equals("toOne")) {
			toOne.joinOn(mainObjJoinProperty, relatedObjJoinProperty, getList(mainType), getList(relatedType));
		} else if (relationshipType.equals("toMany")) {
			toMany.joinOn(mainObjJoinProperty, relatedObjJoinProperty, getList(mainType), getList(relatedType));
		}

		return (PopulateSpec) this;
	}

	public PopulateSpec through(Class<?> throughType, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId) {
		this.relationshipType = "toManyThrough";

		ExtractorResult mainResult = getExtractorResult(mainType);
		ExtractorResult relatedResult = getExtractorResult(relatedType);

		toMany.through(getList(throughType), fkPropertyToMainObjId, fkPropertyToRelatedObjId, mainResult.list(),
				mainResult.idPropertyName(), relatedResult.list(), relatedResult.idPropertyName());
		return (PopulateSpec) this;
	}

	public GetListSpec populate(String propertyToPopulateOnMainObj) {
		if (relationshipType.equals("toOne")) {
			toOne.populate(propertyToPopulateOnMainObj, getList(mainType));
			toOne.populateToOne(getList(mainType), getList(relatedType));
		} else if (relationshipType.equals("toMany")) {
			toMany.populate(propertyToPopulateOnMainObj, getList(mainType));
			toMany.populateToMany(getList(mainType), getList(relatedType));
		} else {
			// toManyThrough
			toMany.populate(propertyToPopulateOnMainObj, getList(mainType));
			toMany.populateToManyThrough(getList(mainType), getList(relatedType));
		}
		return (GetListSpec) this;
	}

	static Method getReadMethod(List<?> list, String propertyName) {
		if (!CollectionUtils.isEmpty(list)) {
			for (Object obj : list) {
				if (obj != null) {
					Method m = ReflectionUtils.findMethod(obj.getClass(),
							GET_PREFIX + StringUtils.capitalize(propertyName));
					if (m == null) {
						m = ReflectionUtils.findMethod(obj.getClass(),
								IS_PREFIX + StringUtils.capitalize(propertyName));
					}
					if (m == null) {
						throw new IllegalArgumentException("Invalid argument. Could not find getter for "
								+ obj.getClass().getName() + "." + propertyName);
					}
					return m;
				}
			}
		}
		return null;
	}

	static Method getWriteMethod(List<?> list, String propertyName) {
		if (!CollectionUtils.isEmpty(list)) {
			for (Object obj : list) {
				if (obj != null) {
					Field field = ReflectionUtils.findField(obj.getClass(), propertyName);
					if (field == null) {
						throw new IllegalArgumentException("Invalid argument. Property name " + propertyName
								+ " does not exist for " + obj.getClass().getName());
					}
					Method m = ReflectionUtils.findMethod(obj.getClass(),
							SET_PREFIX + StringUtils.capitalize(propertyName), field.getType());
					if (m == null) {
						throw new IllegalArgumentException("Invalid argument. Could not find setter for "
								+ obj.getClass().getName() + "." + propertyName);
					}
					return m;
				}
			}
		}
		return null;
	}

	static Class<?> getPropertyType(List<?> list, String propertyName) {
		if (!CollectionUtils.isEmpty(list)) {
			for (Object obj : list) {
				if (obj != null) {
					Field field = ReflectionUtils.findField(obj.getClass(), propertyName);
					if (field != null) {
						return field.getType();
					}
				}
			}
		}
		return null;
	}

	record ExtractorResult(Class<?> entityType, List<?> list, String idPropertyName) {
	}

}
