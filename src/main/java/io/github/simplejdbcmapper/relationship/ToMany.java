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
public class ToMany<T, U> implements ToManySpec, ThroughSpec, PopulateSpec {

	private Method mainObjIdPropertyReadMethod;

	private Method relatedObjIdPropertyReadMethod;

	private Method relatedObjFkPropertyReadMethod;

	private Method mainObjPropertyToPopulateWriteMethod;

	private List<T> mainObjList;

	private List<U> relatedObjList;

	private IntermediateJoiner intermediateJoiner;

	private String relationshipType = "hasMany";

	private ToMany(List<T> mainObjList, List<U> relatedObjList) {
		this.mainObjList = mainObjList;
		this.relatedObjList = relatedObjList;
	}

	static <T, U> ToManySpec toMany(List<T> mainObjList, List<U> relatedObjList) {
		return new ToMany<>(mainObjList, relatedObjList);
	}

	public PopulateSpec joinOn(String mainObjIdProperty, String relatedObjFkProperty) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjFkProperty, "relatedObjFkProperty must not be null");

		this.mainObjIdPropertyReadMethod = Relationship.getReadMethod(mainObjList, mainObjIdProperty);
		this.relatedObjFkPropertyReadMethod = Relationship.getReadMethod(relatedObjList, relatedObjFkProperty);

		return this;
	}

	public ThroughSpec through(List<?> intermediateList, String fkPropertyToMainObjId,
			String fkPropertyToRelatedObjId) {
		Assert.notNull(fkPropertyToMainObjId, "fkPropertyToMainObjId must not be null");
		Assert.notNull(fkPropertyToRelatedObjId, "fkPropertyToRelatedObjId must not be null");

		this.intermediateJoiner = new IntermediateJoiner(intermediateList, fkPropertyToMainObjId,
				fkPropertyToRelatedObjId);

		this.relationshipType = "hasManyThrough";
		return this;
	}

	public PopulateSpec ids(String mainObjIdProperty, String relatedObjIdProperty) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjIdProperty, "relatedObjIdProperty must not be null");

		this.mainObjIdPropertyReadMethod = Relationship.getReadMethod(mainObjList, mainObjIdProperty);
		this.relatedObjIdPropertyReadMethod = Relationship.getReadMethod(relatedObjList, relatedObjIdProperty);

		return this;
	}

	public void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		this.mainObjPropertyToPopulateWriteMethod = Relationship.getWriteMethod(mainObjList, mainObjPropertyToPopulate);

		if ("hasMany".equals(relationshipType)) {
			populateToMany();
		} else {
			populateToManyThrough();
		}
	}

	private void populateToMany() {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		try {
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
					mainObjPropertyToPopulateWriteMethod.invoke(mainObj, populaterList);
				}
			}
		} catch (Exception e) {
			throw new MapperException(e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateToManyThrough() {
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
					mainObjPropertyToPopulateWriteMethod.invoke(mainObj, populaterList);
				}
			}
		} catch (Exception e) {
			throw new MapperException(e.getMessage(), e);
		}
	}

	class IntermediateJoiner {
		@SuppressWarnings("rawtypes")
		// key: mainObjIdValue,
		// value: list of relatedObjIdValues
		private Map<Object, List> mainObjIdMap = new HashMap<>();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public IntermediateJoiner(List<?> intermediateList, String fkPropertyToMainObjId,
				String fkPropertyToRelatedObjId) {
			Method fkPropertyToMainObjIdReadMethod = Relationship.getReadMethod(intermediateList,
					fkPropertyToMainObjId);
			Method fkPropertyToRelatedObjIdReadMethod = Relationship.getReadMethod(intermediateList,
					fkPropertyToRelatedObjId);
			if (CollectionUtils.isEmpty(intermediateList)) {
				return;
			}
			try {
				for (Object intermediateObj : intermediateList) {
					if (intermediateObj != null) {
						Object fkToMainObjIdValue = fkPropertyToMainObjIdReadMethod.invoke(intermediateObj);
						Object fkToRelatedObjIdValue = fkPropertyToRelatedObjIdReadMethod.invoke(intermediateObj);
						if (fkToMainObjIdValue != null && fkToRelatedObjIdValue != null) {
							if (mainObjIdMap.containsKey(fkToMainObjIdValue)) {
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
