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

/**
 * This handles the toManyThrough relationship.
 * 
 * @author Antony Joseph
 */
class ToManyThrough {
	private Class<?> mainType;
	private Class<?> relatedType;
	private Class<?> throughType;
	private String fkPropertyToMainObjId;
	private String fkPropertyToRelatedObjId;
	private String mainObjPropertyToPopulate;

	ToManyThrough(Class<?> mainType, Class<?> relatedType) {
		Assert.notNull(mainType, "mainType must not be null");
		Assert.notNull(relatedType, "relatedType must not be null");
		this.mainType = mainType;
		this.relatedType = relatedType;
	}

	void through(Class<?> throughType, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId) {
		Assert.notNull(throughType, "throughType must not be null");
		Assert.notNull(fkPropertyToMainObjId, "fkPropertyToMainObjId must not be null");
		Assert.notNull(fkPropertyToRelatedObjId, "fkPropertyToRelatedObjId must not be null");
		if (mainType == throughType || relatedType == throughType) {
			throw new IllegalArgumentException("throughType cannot be same as mainType or relatedType.");
		}
		this.throughType = throughType;
		this.fkPropertyToMainObjId = fkPropertyToMainObjId;
		this.fkPropertyToRelatedObjId = fkPropertyToRelatedObjId;
	}

	void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		this.mainObjPropertyToPopulate = mainObjPropertyToPopulate;
	}

	<T, U> void process(List<T> mainObjList, List<U> relatedObjList, List<?> throughList, String mainObjIdProperty,
			String relatedObjIdProperty) {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		validateThrough(mainObjIdProperty, relatedObjIdProperty);
		Method mainObjIdPropertyReadMethod = RelationshipMapper.getReadMethod(mainType, mainObjIdProperty);
		Method relatedObjIdPropertyReadMethod = RelationshipMapper.getReadMethod(relatedType, relatedObjIdProperty);
		Method mainObjPropertyToPopulateWriteMethod = RelationshipMapper.getWriteMethod(mainType,
				mainObjPropertyToPopulate);
		ThroughJoiner throughJoiner = new ThroughJoiner(throughList, fkPropertyToMainObjId, fkPropertyToRelatedObjId,
				throughType);
		try {
			Map<Object, U> idToRelatedObjMap = getIdToRelatedObjMap(relatedObjList, relatedObjIdPropertyReadMethod);
			for (T mainObj : mainObjList) {
				processMainObj(mainObj, idToRelatedObjMap, throughJoiner, mainObjIdPropertyReadMethod,
						mainObjPropertyToPopulateWriteMethod);
			}
		} catch (Exception e) {
			throw new MapperException(e.getMessage(), e);
		}
	}

	private void validateThrough(String mainObjIdProperty, String relatedObjIdProperty) {
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
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <U, T> void processMainObj(T mainObj, Map<Object, U> idToRelatedObjMap, ThroughJoiner throughJoiner,
			Method mainObjIdPropertyReadMethod, Method mainObjPropertyToPopulateWriteMethod)
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
			setMainObjValue(mainObj, populaterList, mainObjPropertyToPopulateWriteMethod);
		}
	}

	private <T, U> void setMainObjValue(T mainObj, List<U> populaterList, Method mainObjPropertyToPopulateWriteMethod) {
		try {
			mainObjPropertyToPopulateWriteMethod.invoke(mainObj, populaterList);
		} catch (Exception e) {
			throw new MapperException(e.getMessage() + ". Invoking " + mainObjPropertyToPopulateWriteMethod
					+ " with value " + populaterList, e);
		}
	}

	private <U> Map<Object, U> getIdToRelatedObjMap(List<U> relatedObjList, Method relatedObjIdPropertyReadMethod)
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

	private static class ThroughJoiner {
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
							List list = mainObjIdMap.get(fkToMainObjIdValue);
							if (list == null) {
								list = new ArrayList();
								list.add(fkToRelatedObjIdValue);
								mainObjIdMap.put(fkToMainObjIdValue, list);
							} else {
								// add to list
								list.add(fkToRelatedObjIdValue);
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
