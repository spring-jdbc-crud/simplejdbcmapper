/*
 * Copyright 2025 the original author or authors.
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
package io.github.simplejdbcmapper.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 
 * Some utility methods.
 * 
 * @author Antony Joseph
 */
public class SimpleJdbcMapperUtils {
	/**
	 * Assigns the 'hasOne' property of the main object with the related object that
	 * matches 'relatedObjJoinPropertyNameTheId' and
	 * 'mainObjJoinPropertyNameTheForeignKey'.
	 * 
	 * <pre>
	 * Example use case: 
	 * 1) Query for a list of employees
	 * 2) Query the departments for these employees. (Could use sjm.findByPropertyValues() for this)
	 * 3) Use populateHasOne() to populate the employee.department property
	 * </pre>
	 * 
	 * @param <T>                                  the type of main object list
	 * @param <U>                                  the type of related object list
	 * @param mainObjList                          The main object list whose
	 *                                             'hasOne' property that needs to
	 *                                             be populated
	 * @param relatedObjList                       the related object list
	 * @param mainObjJoinPropertyNameTheForeignKey The property name on main object
	 *                                             used to find the match. This will
	 *                                             be the foreign key property name
	 * @param relatedObjJoinPropertyNameTheId      The property name on related
	 *                                             object used to find the match.
	 *                                             This will be the id of the
	 *                                             related object.
	 * @param mainObjHasOnePropertyName            The main object 'hasOne' property
	 *                                             to populate
	 */
	public static <T, U> void populateHasOne(List<T> mainObjList, List<U> relatedObjList,
			String mainObjJoinPropertyNameTheForeignKey, String relatedObjJoinPropertyNameTheId,
			String mainObjHasOnePropertyName) {
		Assert.notNull(mainObjJoinPropertyNameTheForeignKey, "mainObjJoinPropertyNameTheForeignKey must not be null");
		Assert.notNull(relatedObjJoinPropertyNameTheId, "relatedObjJoinPropertyNameTheId must not be null");
		Assert.notNull(mainObjHasOnePropertyName, "mainObjHasOnePropertyName must not be null");
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		Map<Object, U> idToRelatedObjMap = new HashMap<>();
		for (U relatedObj : relatedObjList) {
			if (relatedObj != null) {
				BeanWrapper bwRelatedObj = PropertyAccessorFactory.forBeanPropertyAccess(relatedObj);
				Object idPropertyValue = bwRelatedObj.getPropertyValue(relatedObjJoinPropertyNameTheId);
				if (idPropertyValue != null) {
					idToRelatedObjMap.put(idPropertyValue, relatedObj);
				}
			}
		}
		for (T mainObj : mainObjList) {
			if (mainObj != null) {
				BeanWrapper bwMainObj = PropertyAccessorFactory.forBeanPropertyAccess(mainObj);
				Object foreignKeyPropertyValue = bwMainObj.getPropertyValue(mainObjJoinPropertyNameTheForeignKey);
				if (foreignKeyPropertyValue != null) {
					bwMainObj.setPropertyValue(mainObjHasOnePropertyName,
							idToRelatedObjMap.get(foreignKeyPropertyValue));
				}
			}
		}
	}

	/**
	 * Assigns the 'hasMany' property of the main object with the list of related
	 * objects that match 'mainObjJoinPropertyNameTheId' and
	 * 'relatedObjJoinPropertyNameTheForeignKey'.
	 * 
	 * <pre>
	 * Example use case could be: 
	 * 1) Query to get a list of employees
	 * 2) Query the skills of these employees. (Could use sjm.findByPropertyValues() for this)
	 * 3) Use populateHasMany() to populate the employee.skills property
	 * </pre>
	 * 
	 * @param <T>                                     the type of main object list
	 * @param <U>                                     the type of related object
	 *                                                list
	 * @param mainObjList                             The main object list whose
	 *                                                'hasMany' property that needs
	 *                                                to be populated
	 * @param relatedObjList                          the related object list
	 * @param mainObjJoinPropertyNameTheId            The property name on main
	 *                                                object used to find the match.
	 *                                                This will be the id of main
	 *                                                object.
	 * @param relatedObjJoinPropertyNameTheForeignKey The property name on related
	 *                                                object used to find the match.
	 *                                                This will be the foreign key
	 *                                                property name
	 * @param mainObjHasManyPropertyName              The main object 'hasMany'
	 *                                                collection property to
	 *                                                populate
	 */
	public static <T, U> void populateHasMany(List<T> mainObjList, List<U> relatedObjList,
			String mainObjJoinPropertyNameTheId, String relatedObjJoinPropertyNameTheForeignKey,
			String mainObjHasManyPropertyName) {
		Assert.notNull(mainObjJoinPropertyNameTheId, "mainObjJoinPropertyNameTheId must not be null");
		Assert.notNull(relatedObjJoinPropertyNameTheForeignKey,
				"relatedObjJoinPropertyNameTheForeignKey must not be null");
		Assert.notNull(mainObjHasManyPropertyName, "mainObjHasManyPropertyName must not be null");
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		Map<Object, List<U>> foreignKeyToListMap = new HashMap<>();
		for (U relatedObj : relatedObjList) {
			if (relatedObj != null) {
				BeanWrapper bwRelatedObj = PropertyAccessorFactory.forBeanPropertyAccess(relatedObj);
				Object foreignKeyPropertyValue = bwRelatedObj.getPropertyValue(relatedObjJoinPropertyNameTheForeignKey);
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
				BeanWrapper bwMainObj = PropertyAccessorFactory.forBeanPropertyAccess(mainObj);
				Object idPropertyValue = bwMainObj.getPropertyValue(mainObjJoinPropertyNameTheId);
				if (idPropertyValue != null) {
					bwMainObj.setPropertyValue(mainObjHasManyPropertyName, foreignKeyToListMap.get(idPropertyValue));
				}
			}
		}
	}

	/**
	 * Splits the list into multiple lists by chunk size. Can be used to split the
	 * sql IN clauses since some databases have a limitation on 'IN' clause entries
	 * and size
	 *
	 * @param <T>       the type of list
	 * @param list      the list to chunk
	 * @param chunkSize The size of chunk
	 * @return Collection of lists broken down by chunkSize
	 */
	public static <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
		List<List<T>> chunks = new ArrayList<>();
		if (list != null) {
			for (int i = 0; i < list.size(); i += chunkSize) {
				chunks.add(list.subList(i, Math.min(i + chunkSize, list.size())));
			}
		}
		return chunks;
	}

}