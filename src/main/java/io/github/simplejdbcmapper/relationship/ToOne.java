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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * This handles the ToOne relationship.
 * 
 * @param <T> the type of the main object
 * @param <U> the type of the related object
 * 
 * @author Antony Joseph
 */
public class ToOne<T, U> implements ToOneSpec<T, U>, PopulateSpec {

	private String mainObjJoinProperty;
	private String relatedObjJoinProperty;

	private Method mainObjJoinPropertyReadMethod;
	private Method relatedObjJoinPropertyReadMethod;

	private String mainObjPropertyToPopulate;
	private Method mainObjPropertyToPopulateWriteMethod;

	private List<T> mainObjList;
	private List<U> relatedObjList;

	// private BeanWrapper bwMainObj; // used for validation of property names

	// private BeanWrapper bwRelatedObj; // used for validation of property names;

	private ToOne(List<T> mainObjList, List<U> relatedObjList) {
		this.mainObjList = mainObjList;
		this.relatedObjList = relatedObjList;
		// bwMainObj = getBeanWrapper(mainObjList);
		// bwRelatedObj = getBeanWrapper(relatedObjList);
	}

	static <T, U> ToOneSpec<T, U> toOne(List<T> mainObjList, List<U> relatedObjList) {
		return new ToOne<>(mainObjList, relatedObjList);
	}

	public PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty) {
		Assert.notNull(mainObjJoinProperty, "mainObjJoinProperty must not be null");
		Assert.notNull(relatedObjJoinProperty, "relatedObjJoinProperty must not be null");

		this.mainObjJoinPropertyReadMethod = Relationship.getReadMethod(mainObjList, mainObjJoinProperty);
		this.relatedObjJoinPropertyReadMethod = Relationship.getReadMethod(relatedObjList, relatedObjJoinProperty);

		this.mainObjJoinProperty = mainObjJoinProperty;
		this.relatedObjJoinProperty = relatedObjJoinProperty;
		return this;
	}

	public void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");

		this.mainObjPropertyToPopulateWriteMethod = Relationship.getWriteMethod(mainObjList, mainObjPropertyToPopulate);
		this.mainObjPropertyToPopulate = mainObjPropertyToPopulate;

		populateToOne();
	}

	private void populateToOne() {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		try {
			Map<Object, Object> joinPropToMainObjMap = new HashMap<>();
			for (T mainObj : mainObjList) {
				if (mainObj != null) {
					Object mainObjJoinPropertyValue = mainObjJoinPropertyReadMethod.invoke(mainObj);
					if (mainObjJoinPropertyValue != null) {
						joinPropToMainObjMap.put(mainObjJoinPropertyValue, mainObj);
					}
				}
			}

			Map<Object, U> joinPropToRelatedObjMap = new HashMap<>();
			for (U relatedObj : relatedObjList) {
				if (relatedObj != null) {
					Object relatedObjJoinPropertyValue = relatedObjJoinPropertyReadMethod.invoke(relatedObj);
					if (relatedObjJoinPropertyValue != null) {
						joinPropToRelatedObjMap.put(relatedObjJoinPropertyValue, relatedObj);
					}
				}
			}
			for (Map.Entry<Object, Object> entry : joinPropToMainObjMap.entrySet()) {
				Object mainObjJoinPropertyValue = entry.getKey();
				Object mainObj = entry.getValue();
				U relatedObj = joinPropToRelatedObjMap.get(mainObjJoinPropertyValue);
				mainObjPropertyToPopulateWriteMethod.invoke(mainObj, relatedObj);
			}
		} catch (Exception e) {
			throw new MapperException(e.getMessage(), e);
		}
	}

}
