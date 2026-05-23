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
 * This handles the toManyThrough relationship.
 * 
 * @author Antony Joseph
 */
class ToManyThroughLegacy {

	private Class<?> mainType;
	private Class<?> relatedType;
	private List<ExtractorEntityResult> results = new ArrayList<>();

	private Method mainObjIdPropertyReadMethod;

	private Method relatedObjIdPropertyReadMethod;

	private Method mainObjPropertyToPopulateWriteMethod;

	private ThroughJoiner throughJoiner;

	ToManyThroughLegacy(Class<?> mainType, Class<?> relatedType, List<ExtractorEntityResult> results) {
		this.mainType = mainType;
		this.relatedType = relatedType;
		this.results = results;
	}

	void through(Class<?> throughType, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId) {
		Assert.notNull(throughType, "throughType must not be null");
		Assert.notNull(fkPropertyToMainObjId, "fkPropertyToMainObjId must not be null");
		Assert.notNull(fkPropertyToRelatedObjId, "fkPropertyToRelatedObjId must not be null");
		if (mainType == throughType || relatedType == throughType) {
			throw new IllegalArgumentException("throughType cannot be same as mainType or relatedType.");
		}

		// will throw an exception for invalid type
		List<?> throughList = RelationshipMapper.getList(throughType, results);

		ExtractorEntityResult mainResult = RelationshipMapper.getExtractorEntityResult(mainType, results);
		String mainObjIdProperty = mainResult.idPropertyName();
		ExtractorEntityResult relatedResult = RelationshipMapper.getExtractorEntityResult(relatedType, results);
		String relatedObjIdProperty = relatedResult.idPropertyName();

		Class<?> mainObjIdPropertyType = RelationshipMapper.getPropertyType(mainType, mainObjIdProperty);
		Class<?> fkPropertyToMainObjIdType = RelationshipMapper.getPropertyType(throughType, fkPropertyToMainObjId);
		if (mainObjIdPropertyType != fkPropertyToMainObjIdType) {
			throw new IllegalArgumentException("Conflicting property types. Property type of "
					+ mainType.getSimpleName() + "." + mainObjIdProperty + " and " + throughType.getSimpleName() + "."
					+ fkPropertyToMainObjId + " are not the same.");
		}

		Class<?> relatedObjIdPropertyType = RelationshipMapper.getPropertyType(relatedType, relatedObjIdProperty);
		Class<?> fkPropertyToRelatedObjIdType = RelationshipMapper.getPropertyType(throughType,
				fkPropertyToRelatedObjId);
		if (relatedObjIdPropertyType != fkPropertyToRelatedObjIdType) {
			throw new IllegalArgumentException("Conflicting property types. Property type of "
					+ relatedType.getSimpleName() + "." + relatedObjIdProperty + " and " + throughType.getSimpleName()
					+ "." + fkPropertyToRelatedObjId + " are not the same.");
		}

		this.mainObjIdPropertyReadMethod = RelationshipMapper.getReadMethod(mainType, mainObjIdProperty);
		this.relatedObjIdPropertyReadMethod = RelationshipMapper.getReadMethod(relatedType, relatedObjIdProperty);

		this.throughJoiner = new ThroughJoiner(throughList, fkPropertyToMainObjId, fkPropertyToRelatedObjId,
				throughType);

	}

	void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		this.mainObjPropertyToPopulateWriteMethod = RelationshipMapper.getWriteMethod(mainType,
				mainObjPropertyToPopulate);

		List<?> mainList = RelationshipMapper.getList(mainType, results);
		List<?> relatedList = RelationshipMapper.getList(relatedType, results);
		processToManyThrough(mainList, relatedList);
	}

	private <T, U> void processToManyThrough(List<T> mainObjList, List<U> relatedObjList) {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		try {
			Map<Object, U> idToRelatedObjMap = getIdToRelatedObjMap(relatedObjList);
			for (T mainObj : mainObjList) {
				processMainObj(mainObj, idToRelatedObjMap);
			}
		} catch (Exception e) {
			throw new MapperException(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <U, T> void processMainObj(T mainObj, Map<Object, U> idToRelatedObjMap)
			throws IllegalAccessException, InvocationTargetException {
		if (mainObj != null) {
			Object mainObjIdValue = mainObjIdPropertyReadMethod.invoke(mainObj);
			List relatedObjIdListFromJoiner = throughJoiner.getRelatedObjIds(mainObjIdValue);
			List<U> populaterList = new ArrayList<>();
			if (!CollectionUtils.isEmpty(relatedObjIdListFromJoiner)) {
				for (Object relatedObjId : relatedObjIdListFromJoiner) {
					U relatedObj = idToRelatedObjMap.get(relatedObjId);
					if (relatedObj != null) {
						populaterList.add(relatedObj);
					}
				}
			}
			setMainObjValue(mainObj, populaterList);
		}
	}

	private <T, U> void setMainObjValue(T mainObj, List<U> populaterList) {
		try {
			mainObjPropertyToPopulateWriteMethod.invoke(mainObj, populaterList);
		} catch (Exception e) {
			throw new MapperException(e.getMessage() + ". Invoking " + mainObjPropertyToPopulateWriteMethod
					+ " with value " + populaterList, e);
		}
	}

	private <U> Map<Object, U> getIdToRelatedObjMap(List<U> relatedObjList)
			throws IllegalAccessException, InvocationTargetException {
		// relatedObjId - relatedObj
		Map<Object, U> idToRelatedObjMap = new HashMap<>();
		for (U relatedObj : relatedObjList) {
			if (relatedObj != null) {
				Object relatedObjIdValue = relatedObjIdPropertyReadMethod.invoke(relatedObj);
				if (relatedObjIdValue != null) {
					idToRelatedObjMap.put(relatedObjIdValue, relatedObj);
				}
			}
		}
		return idToRelatedObjMap;
	}

	private class ThroughJoiner {
		@SuppressWarnings("rawtypes")
		// key: fkToMainObjIdValue,
		// value: list of fkToRelatedObjIdValue
		private Map<Object, List> mainObjIdMap = new HashMap<>();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ThroughJoiner(List<?> throughList, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId,
				Class<?> throughType) {
			if (CollectionUtils.isEmpty(throughList)) {
				return;
			}
			Method fkPropertyToMainObjIdReadMethod = RelationshipMapper.getReadMethod(throughType,
					fkPropertyToMainObjId);
			Method fkPropertyToRelatedObjIdReadMethod = RelationshipMapper.getReadMethod(throughType,
					fkPropertyToRelatedObjId);
			try {
				for (Object throughObj : throughList) {
					if (throughObj != null) {
						Object fkToMainObjIdValue = fkPropertyToMainObjIdReadMethod.invoke(throughObj);
						Object fkToRelatedObjIdValue = fkPropertyToRelatedObjIdReadMethod.invoke(throughObj);
						if (fkToMainObjIdValue != null && fkToRelatedObjIdValue != null) {
							if (mainObjIdMap.containsKey(fkToMainObjIdValue)) {
								// add to list
								mainObjIdMap.get(fkToMainObjIdValue).add(fkToRelatedObjIdValue);
							} else {
								List list = new ArrayList();
								list.add(fkToRelatedObjIdValue);
								mainObjIdMap.put(fkToMainObjIdValue, list);
							}
						}
					}
				}
			} catch (Exception e) {
				throw new MapperException(e.getMessage(), e);
			}
		}

		@SuppressWarnings("rawtypes")
		List getRelatedObjIds(Object mainObjId) {
			return mainObjIdMap.get(mainObjId);
		}
	}

}
