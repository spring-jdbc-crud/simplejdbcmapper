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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * This handles the toMany relationship including toMany through an intermediate
 * table.
 * 
 * @param <T> The main object type
 * @param <U> The related object type
 * 
 * @author Antony Joseph
 */
public class ToMany<T, U> implements ToManySpec<T, U>, ThroughSpec, PopulateSpec {

	private String mainObjIdProperty;

	private String relatedObjIdProperty;

	private String relatedObjFkProperty;

	private String mainObjPropertyToPopulate;

	private List<T> mainObjList;

	private List<U> relatedObjList;

	private BeanWrapper bwMainObj; // used for validation of property names

	private BeanWrapper bwRelatedObj; // used for validation of property names;

	private IntermediateJoiner intermediateJoiner;

	private String relationshipType = "hasMany";

	private ToMany(List<T> mainObjList, List<U> relatedObjList) {
		this.mainObjList = mainObjList;
		this.relatedObjList = relatedObjList;
		bwMainObj = getBeanWrapper(mainObjList);
		bwRelatedObj = getBeanWrapper(relatedObjList);
	}

	static <T, U> ToManySpec<T, U> toMany(List<T> mainObjList, List<U> relatedObjList) {
		return new ToMany<>(mainObjList, relatedObjList);
	}

	public PopulateSpec joinOn(String mainObjIdProperty, String relatedObjFkProperty) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjFkProperty, "relatedObjFkProperty must not be null");

		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjIdProperty)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + mainObjIdProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}

		if (bwRelatedObj != null && !bwRelatedObj.isReadableProperty(relatedObjFkProperty)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + relatedObjFkProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}

		this.mainObjIdProperty = mainObjIdProperty;
		this.relatedObjFkProperty = relatedObjFkProperty;
		return this;
	}

	public ThroughSpec through(List<?> intermediateList, String fkPropertyToMainObjId,
			String fkPropertyToRelatedObjId) {
		Assert.notNull(fkPropertyToMainObjId, "fkPropertyToMainObjId must not be null");
		Assert.notNull(fkPropertyToRelatedObjId, "fkPropertyToRelatedObjId must not be null");

		BeanWrapper bw = getBeanWrapper(intermediateList);
		if (bw != null && !bw.isReadableProperty(fkPropertyToMainObjId)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + fkPropertyToMainObjId
					+ " does not exist for " + bw.getWrappedClass().getName());
		}
		if (bw != null && !bw.isReadableProperty(fkPropertyToRelatedObjId)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + fkPropertyToRelatedObjId
					+ " does not exist for " + bw.getWrappedClass().getName());
		}

		this.intermediateJoiner = new IntermediateJoiner(intermediateList, fkPropertyToMainObjId,
				fkPropertyToRelatedObjId);

		this.relationshipType = "hasManyThrough";
		return this;
	}

	public PopulateSpec ids(String mainObjIdProperty, String relatedObjIdProperty) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjIdProperty, "relatedObjIdProperty must not be null");
		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjIdProperty)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + mainObjIdProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}
		if (bwRelatedObj != null && !bwRelatedObj.isReadableProperty(relatedObjIdProperty)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + relatedObjIdProperty
					+ " does not exist for " + bwRelatedObj.getWrappedClass().getName());
		}

		this.mainObjIdProperty = mainObjIdProperty;
		this.relatedObjIdProperty = relatedObjIdProperty;
		return this;
	}

	public void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjPropertyToPopulate)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + mainObjPropertyToPopulate
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}
		this.mainObjPropertyToPopulate = mainObjPropertyToPopulate;
		if ("hasMany".equals(relationshipType)) {
			populateToMany();
		} else {
			populateToManyThrough();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateToMany() {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		Map<Object, List<U>> foreignKeyToListMap = new HashMap<>();
		for (Object relatedObj : relatedObjList) {
			if (relatedObj != null) {
				BeanWrapper bwRelatedObj = PropertyAccessorFactory.forBeanPropertyAccess(relatedObj);
				Object foreignKeyPropertyValue = bwRelatedObj.getPropertyValue(relatedObjFkProperty);
				if (foreignKeyPropertyValue != null) {
					List list = foreignKeyToListMap.get(foreignKeyPropertyValue);
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
				BeanWrapper bwMainObj = PropertyAccessorFactory.forBeanPropertyAccess(mainObj);
				Object idPropertyValue = bwMainObj.getPropertyValue(mainObjIdProperty);
				List populaterList = foreignKeyToListMap.get(idPropertyValue);
				if (populaterList == null) {
					populaterList = new ArrayList();
				}
				bwMainObj.setPropertyValue(mainObjPropertyToPopulate, populaterList);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateToManyThrough() {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		// relatedObjId - relatedObj
		Map<Object, U> idToRelatedObjMap = new HashMap<>();
		for (U relatedObj : relatedObjList) {
			if (relatedObj != null) {
				BeanWrapper bwRelatedObj = PropertyAccessorFactory.forBeanPropertyAccess(relatedObj);
				Object relatedObjIdValue = bwRelatedObj.getPropertyValue(relatedObjIdProperty);
				if (relatedObjIdValue != null) {
					idToRelatedObjMap.put(relatedObjIdValue, relatedObj);
				}
			}
		}
		for (T mainObj : mainObjList) {
			if (mainObj != null) {
				BeanWrapper bwMainObj = PropertyAccessorFactory.forBeanPropertyAccess(mainObj);
				Object mainObjIdValue = bwMainObj.getPropertyValue(mainObjIdProperty);
				List relatedObjIdListFromJoiner = intermediateJoiner.getRelatedObjIds(mainObjIdValue);
				List populaterList = new ArrayList();
				if (!CollectionUtils.isEmpty(relatedObjIdListFromJoiner)) {
					for (Object relatedObjId : relatedObjIdListFromJoiner) {
						Object relatedObj = idToRelatedObjMap.get(relatedObjId);
						if (relatedObj != null) {
							populaterList.add(relatedObj);
						}
					}
				}
				bwMainObj.setPropertyValue(mainObjPropertyToPopulate, populaterList);
			}
		}
	}

	private BeanWrapper getBeanWrapper(List<?> list) {
		BeanWrapper bw = null;
		if (!CollectionUtils.isEmpty(list)) {
			for (Object obj : list) {
				if (obj != null) {
					bw = PropertyAccessorFactory.forBeanPropertyAccess(obj);
					break;
				}
			}
		}
		return bw;
	}

	class IntermediateJoiner {
		@SuppressWarnings("rawtypes")
		// key: mainObjIdValue,
		// value: list of relatedObjIdValues
		private Map<Object, List> mainObjIdMap = new HashMap<>();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public IntermediateJoiner(List<?> intermediateList, String fkPropertyToMainObjId,
				String fkPropertyToRelatedObjId) {
			if (CollectionUtils.isEmpty(intermediateList)) {
				return;
			}
			for (Object intermediateObj : intermediateList) {
				if (intermediateObj != null) {
					BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(intermediateObj);
					Object fkToMainObjIdValue = bw.getPropertyValue(fkPropertyToMainObjId);
					Object fkToRelatedObjIdValue = bw.getPropertyValue(fkPropertyToRelatedObjId);
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

		}

		@SuppressWarnings("rawtypes")
		List getRelatedObjIds(Object mainObjId) {
			return mainObjIdMap.get(mainObjId);
		}
	}
}
