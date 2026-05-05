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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class RelationshipExtractor implements RelationshipSpec {
	private final Map<Class<?>, List<?>> map = new HashMap<>();

	public <T> void putList(Class<T> type, List<T> list) {
		map.put(type, list);
	}

	public <T> List<T> getList(Class<T> type) {
		return (List<T>) map.get(type);
	}

	static final String IS_PREFIX = "is";
	static final String GET_PREFIX = "get";
	static final String SET_PREFIX = "set";

	private Class<?> mainType;

	private List<?> mainObjList;

	public RelationshipExtractor() {
	}

	private RelationshipExtractor(List<?> mainObjList) {
		this.mainObjList = mainObjList;
	}

	/**
	 * Start of creating a relationship. The main object is the object whose
	 * property will be populated using the api.
	 * 
	 * @param <T>         the main object type
	 * @param mainObjList the main object list
	 * @return A relationship
	 */
	public RelationshipSpec type(Class<?> mainType) {
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
		return ToOne.toOne(getList(mainType), getList(relatedType));
	}

	/**
	 * A toMany relationship
	 * 
	 * @param <U>            the type of the related object
	 * 
	 * @param relatedObjList the list of related objects
	 * 
	 * @return The ToManySpec
	 */
	public <U> ToManySpec toMany(Class<U> relatedType) {
		return ToMany.toMany(getList(mainType), getList(relatedType));
	}

	static Method getReadMethod(List<?> list, String propertyName) {
		if (!CollectionUtils.isEmpty(list)) {
			for (Object obj : list) {
				if (obj != null) {
					Method m = ReflectionUtils.findMethod(obj.getClass(),
							Relationship.GET_PREFIX + StringUtils.capitalize(propertyName));
					if (m == null) {
						m = ReflectionUtils.findMethod(obj.getClass(),
								Relationship.IS_PREFIX + StringUtils.capitalize(propertyName));
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
							Relationship.SET_PREFIX + StringUtils.capitalize(propertyName), field.getType());
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

}
