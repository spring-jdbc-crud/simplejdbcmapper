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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * This handles the toMany relationship including toMany through an intermediate
 * table.
 * 
 * @param <T> The main object type
 * @param <U> The related object type
 * 
 * @author Antony Joseph
 */
public class ToMany {

	private Method mainObjIdPropertyReadMethod;

	private Method relatedObjIdPropertyReadMethod;

	private Method relatedObjFkPropertyReadMethod;

	private Method mainObjPropertyToPopulateWriteMethod;

	private IntermediateJoiner intermediateJoiner;

	void joinOn(String mainObjIdProperty, String relatedObjFkProperty, Class<?> mainType, Class<?> relatedType) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjFkProperty, "relatedObjFkProperty must not be null");

		this.mainObjIdPropertyReadMethod = Relationship.getReadMethod(mainType, mainObjIdProperty);
		this.relatedObjFkPropertyReadMethod = Relationship.getReadMethod(relatedType, relatedObjFkProperty);

		Class<?> mainObjIdPropertyType = Relationship.getPropertyType(mainType, mainObjIdProperty);
		Class<?> relatedObjFkPropertyType = Relationship.getPropertyType(relatedType, relatedObjFkProperty);
		if (mainObjIdPropertyType != relatedObjFkPropertyType) {
			throw new IllegalArgumentException("Property types of " + mainObjIdProperty + " on main object and "
					+ relatedObjFkProperty + " on related object are not the same.");
		}
	}

	void through(List<?> intermediateList, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId,
			Class<?> mainType, String mainObjIdProperty, Class<?> relatedType, String relatedObjIdProperty,
			Class<?> intermediateType) {
		Assert.notNull(fkPropertyToMainObjId, "fkPropertyToMainObjId must not be null");
		Assert.notNull(fkPropertyToRelatedObjId, "fkPropertyToRelatedObjId must not be null");

		this.mainObjIdPropertyReadMethod = Relationship.getReadMethod(mainType, mainObjIdProperty);
		this.relatedObjIdPropertyReadMethod = Relationship.getReadMethod(relatedType, relatedObjIdProperty);

		this.intermediateJoiner = new IntermediateJoiner(intermediateList, fkPropertyToMainObjId,
				fkPropertyToRelatedObjId, intermediateType);

	}

	void populate(String mainObjPropertyToPopulate, Class<?> mainType) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		this.mainObjPropertyToPopulateWriteMethod = Relationship.getWriteMethod(mainType, mainObjPropertyToPopulate);
	}

	<T, U> void populateToMany(List<T> mainObjList, List<U> relatedObjList) {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		try {
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
			for (T mainObj : mainObjList) {
				if (mainObj != null) {
					Object mainObjIdPropertyValue = mainObjIdPropertyReadMethod.invoke(mainObj);
					List<U> populaterList = foreignKeyToListMap.get(mainObjIdPropertyValue);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	<T, U> void populateToManyThrough(List<T> mainObjList, List<U> relatedObjList) {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		try {
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
			for (T mainObj : mainObjList) {
				if (mainObj != null) {
					Object mainObjIdValue = mainObjIdPropertyReadMethod.invoke(mainObj);
					List relatedObjIdListFromJoiner = intermediateJoiner.getRelatedObjIds(mainObjIdValue);
					List<U> populaterList = new ArrayList<>();
					if (!CollectionUtils.isEmpty(relatedObjIdListFromJoiner)) {
						for (Object relatedObjId : relatedObjIdListFromJoiner) {
							U relatedObj = idToRelatedObjMap.get(relatedObjId);
							if (relatedObj != null) {
								populaterList.add(relatedObj);
							}
						}
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

	class IntermediateJoiner {
		@SuppressWarnings("rawtypes")
		// key: fkToMainObjIdValue,
		// value: list of fkToRelatedObjIdValue
		private Map<Object, List> mainObjIdMap = new HashMap<>();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public IntermediateJoiner(List<?> intermediateList, String fkPropertyToMainObjId,
				String fkPropertyToRelatedObjId, Class<?> intermediateType) {
			if (CollectionUtils.isEmpty(intermediateList)) {
				return;
			}
			Method fkPropertyToMainObjIdReadMethod = Relationship.getReadMethod(intermediateType,
					fkPropertyToMainObjId);
			Method fkPropertyToRelatedObjIdReadMethod = Relationship.getReadMethod(intermediateType,
					fkPropertyToRelatedObjId);
			try {
				for (Object intermediateObj : intermediateList) {
					if (intermediateObj != null) {
						Object fkToMainObjIdValue = fkPropertyToMainObjIdReadMethod.invoke(intermediateObj);
						Object fkToRelatedObjIdValue = fkPropertyToRelatedObjIdReadMethod.invoke(intermediateObj);
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
