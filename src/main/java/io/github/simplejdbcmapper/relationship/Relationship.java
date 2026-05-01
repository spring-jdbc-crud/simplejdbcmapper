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

import java.util.List;

/**
 * The starting point for any relationship assignment. It works with the lists
 * provided and depending on the arguments to the fluent api methods, populates
 * the target property. The last method in the fluent flow is populate() which
 * triggers the processing.
 * 
 * <p>
 * Processing does not access the database or use SimpleJdbcMapper.
 * 
 * @author Antony Joseph
 */
public class Relationship<T> implements RelationshipSpec<T> {

	private List<T> mainObjList;

	private Relationship(List<T> mainObjList) {
		this.mainObjList = mainObjList;
	}

	/**
	 * Start of creating a relationship. The main object is the object whose
	 * property will be populated using the fluent api.
	 * 
	 * @param <T>         the main object type
	 * @param mainObjList the main object list
	 * @return A relationship
	 */
	public static <T> RelationshipSpec<T> mainList(List<T> mainObjList) {
		return new Relationship<>(mainObjList);
	}

	/**
	 * A toOne relationship
	 * 
	 * @param <U>            the type of the related object
	 * 
	 * @param relatedObjList the list of related objects
	 * 
	 */
	public <U> ToOneSpec<T, U> toOneList(List<U> relatedObjList) {
		return ToOne.toOne(mainObjList, relatedObjList);
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
	public <U> ToManySpec<T, U> toManyList(List<U> relatedObjList) {
		return ToMany.toMany(mainObjList, relatedObjList);
	}

}
