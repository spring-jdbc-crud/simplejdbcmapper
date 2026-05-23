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
 * This handles the toOne relationship.
 * 
 * @author Antony Joseph
 */
class ToOneLegacy {

	private Class<?> mainType;
	private Class<?> relatedType;
	private List<ExtractorEntityResult> results = new ArrayList<>();

	private Method mainObjJoinPropertyReadMethod;
	private Method relatedObjJoinPropertyReadMethod;

	private Method mainObjPropertyToPopulateWriteMethod;

	ToOneLegacy(Class<?> mainType, Class<?> relatedType, List<ExtractorEntityResult> results) {
		this.mainType = mainType;
		this.relatedType = relatedType;
		this.results = results;
	}

	void joinOn(String mainObjJoinProperty, String relatedObjJoinProperty) {
		Assert.notNull(mainObjJoinProperty, "mainObjJoinProperty must not be null");
		Assert.notNull(relatedObjJoinProperty, "relatedObjJoinProperty must not be null");

		Class<?> mainObjJoinPropertyType = RelationshipMapper.getPropertyType(mainType, mainObjJoinProperty);
		Class<?> relatedObjJoinPropertyType = RelationshipMapper.getPropertyType(relatedType, relatedObjJoinProperty);
		if (mainObjJoinPropertyType != relatedObjJoinPropertyType) {
			throw new IllegalArgumentException("Conflicting property types. Property type of "
					+ mainType.getSimpleName() + "." + mainObjJoinProperty + " and " + relatedType.getSimpleName() + "."
					+ relatedObjJoinProperty + " are not the same.");
		}

		this.mainObjJoinPropertyReadMethod = RelationshipMapper.getReadMethod(mainType, mainObjJoinProperty);
		this.relatedObjJoinPropertyReadMethod = RelationshipMapper.getReadMethod(relatedType, relatedObjJoinProperty);

	}

	void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		this.mainObjPropertyToPopulateWriteMethod = RelationshipMapper.getWriteMethod(mainType,
				mainObjPropertyToPopulate);

		processToOne(getList(mainType), getList(relatedType));
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> getList(Class<T> type) {
		ExtractorEntityResult result = RelationshipMapper.getExtractorEntityResult(type, results);
		return (List<T>) result.list();
	}

	private <T, U> void processToOne(List<T> mainObjList, List<U> relatedObjList) {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		try {
			Map<Object, U> joinPropToRelatedObjMap = getJoinPropToRelatedObjMap(relatedObjList);
			for (T mainObj : mainObjList) {
				if (mainObj != null) {
					Object mainObjJoinPropertyValue = mainObjJoinPropertyReadMethod.invoke(mainObj);
					U relatedObj = joinPropToRelatedObjMap.get(mainObjJoinPropertyValue);
					try {
						mainObjPropertyToPopulateWriteMethod.invoke(mainObj, relatedObj);
					} catch (Exception e) {
						throw new MapperException(e.getMessage() + ". Invoking " + mainObjPropertyToPopulateWriteMethod
								+ " with value " + relatedObj, e);
					}
				}
			}
		} catch (Exception e) {
			throw new MapperException(e.getMessage(), e);
		}
	}

	private <U> Map<Object, U> getJoinPropToRelatedObjMap(List<U> relatedObjList)
			throws IllegalAccessException, InvocationTargetException {
		Map<Object, U> joinPropToRelatedObjMap = new HashMap<>();
		for (U relatedObj : relatedObjList) {
			if (relatedObj != null) {
				Object relatedObjJoinPropertyValue = relatedObjJoinPropertyReadMethod.invoke(relatedObj);
				if (relatedObjJoinPropertyValue != null) {
					joinPropToRelatedObjMap.put(relatedObjJoinPropertyValue, relatedObj);
				}
			}
		}
		return joinPropToRelatedObjMap;
	}

}
