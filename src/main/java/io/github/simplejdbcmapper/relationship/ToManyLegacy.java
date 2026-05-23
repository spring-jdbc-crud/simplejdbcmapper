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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.relationship.RelationshipMapper.ExtractorEntityResult;

/**
 * This handles the toMany relationship.
 * 
 * @author Antony Joseph
 */
class ToManyLegacy {

	private Method mainObjIdPropertyReadMethod;

	private Method relatedObjFkPropertyReadMethod;

	private Method mainObjPropertyToPopulateWriteMethod;

	private Class<?> mainType;
	private Class<?> relatedType;
	private List<ExtractorEntityResult> results = new ArrayList<>();

	ToManyLegacy(Class<?> mainType, Class<?> relatedType, List<ExtractorEntityResult> results) {
		this.mainType = mainType;
		this.relatedType = relatedType;
		this.results = results;
	}

	void joinOn(String mainObjIdProperty, String relatedObjFkProperty) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjFkProperty, "relatedObjFkProperty must not be null");

		Class<?> mainObjIdPropertyType = RelationshipMapper.getPropertyType(mainType, mainObjIdProperty);
		Class<?> relatedObjFkPropertyType = RelationshipMapper.getPropertyType(relatedType, relatedObjFkProperty);
		if (mainObjIdPropertyType != relatedObjFkPropertyType) {
			throw new IllegalArgumentException("Conflicting property types. Property type of "
					+ mainType.getSimpleName() + "." + mainObjIdProperty + " and " + relatedType.getSimpleName() + "."
					+ relatedObjFkProperty + " are not the same.");
		}
		this.mainObjIdPropertyReadMethod = RelationshipMapper.getReadMethod(mainType, mainObjIdProperty);
		this.relatedObjFkPropertyReadMethod = RelationshipMapper.getReadMethod(relatedType, relatedObjFkProperty);
	}

	void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		this.mainObjPropertyToPopulateWriteMethod = RelationshipMapper.getWriteMethod(mainType,
				mainObjPropertyToPopulate);

		List<?> mainList = RelationshipMapper.getList(mainType, results);
		List<?> relatedList = RelationshipMapper.getList(relatedType, results);
		processToMany(mainList, relatedList);
	}

	private <T, U> void processToMany(List<T> mainObjList, List<U> relatedObjList) {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		try {
			Map<Object, List<U>> fkToRelatedObjListMap = getFkToRelatedObjListMap(relatedObjList);
			for (T mainObj : mainObjList) {
				if (mainObj != null) {
					Object mainObjIdPropertyValue = mainObjIdPropertyReadMethod.invoke(mainObj);
					List<U> populaterList = fkToRelatedObjListMap.get(mainObjIdPropertyValue);
					if (populaterList == null) {
						populaterList = new ArrayList<>();
					}
					try {
						mainObjPropertyToPopulateWriteMethod.invoke(mainObj, populaterList);
					} catch (Exception e) {
						throw new MapperException(e.getMessage() + ". Invoking " + mainObjPropertyToPopulateWriteMethod
								+ " with value " + populaterList, e);
					}
				}
			}
		} catch (Exception e) {
			throw new MapperException(e.getMessage(), e);
		}
	}

	private <U> Map<Object, List<U>> getFkToRelatedObjListMap(List<U> relatedObjList)
			throws IllegalAccessException, InvocationTargetException {
		// relatedObjFk - List of relatedObj
		Map<Object, List<U>> foreignKeyToListMap = new HashMap<>();
		for (U relatedObj : relatedObjList) {
			if (relatedObj != null) {
				Object foreignKeyPropertyValue = relatedObjFkPropertyReadMethod.invoke(relatedObj);
				if (foreignKeyPropertyValue != null) {
					List<U> list = foreignKeyToListMap.get(foreignKeyPropertyValue);
					if (list == null) {
						list = new ArrayList<>();
						list.add(relatedObj);
						foreignKeyToListMap.put(foreignKeyPropertyValue, list);
					} else {
						list.add(relatedObj);
					}
				}
			}
		}
		return foreignKeyToListMap;
	}

}
