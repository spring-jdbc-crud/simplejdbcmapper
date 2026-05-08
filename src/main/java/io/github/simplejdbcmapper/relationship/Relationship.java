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
import java.util.List;

import org.springframework.util.Assert;

import io.github.simplejdbcmapper.relationship.RelationshipMapper.ExtractorEntityResult;

/**
 * The relationship. It uses the arguments provided through its API and
 * populates the target property.
 * 
 * <p>
 * Does not access database or use SimpleJdbcMapper
 * 
 * <p>
 * For more details on its use see the <a href=
 * "https://github.com/spring-jdbc-crud/simplejdbcmapper#populating-relationships-from-custom-queries">documentation</a>
 * 
 * 
 * @author Antony Joseph
 */
public class Relationship implements RelationshipSpec, ToManySpec, ToOneSpec, PopulateSpec, GetListSpec {

	static final String TO_MANY = "toMany";
	static final String TO_ONE = "toOne";
	static final String TO_MANY_THROUGH = "toManyThrough";

	private Class<?> mainType;
	private Class<?> relatedType;
	private List<ExtractorEntityResult> results = new ArrayList<>();

	private ToOne toOne;
	private ToMany toMany;
	private ToManyThrough toManyThrough;

	private String relationshipType = TO_ONE;

	private Relationship(Class<?> mainType, List<ExtractorEntityResult> results) {
		this.mainType = mainType;
		this.results = results;
	}

	static RelationshipSpec newInstance(Class<?> type, List<ExtractorEntityResult> results) {
		return new Relationship(type, results);
	}

	public ToOneSpec toOne(Class<?> relatedType) {
		Assert.notNull(relatedType, "relatedType must not be null");
		// will throw an exception for invalid type
		getList(relatedType);

		this.relationshipType = TO_ONE;
		this.toOne = new ToOne(mainType, relatedType, results);
		return this;
	}

	public ToManySpec toMany(Class<?> relatedType) {
		Assert.notNull(relatedType, "relatedType must not be null");
		// will throw an exception for invalid type
		getList(relatedType);

		this.relatedType = relatedType;
		this.toMany = new ToMany(mainType, relatedType, results);
		this.relationshipType = TO_MANY;
		return this;
	}

	public PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty) {
		if (relationshipType.equals(TO_ONE)) {
			toOne.joinOn(mainObjJoinProperty, relatedObjJoinProperty);
		} else if (relationshipType.equals(TO_MANY)) {
			toMany.joinOn(mainObjJoinProperty, relatedObjJoinProperty);
		}
		return this;
	}

	public PopulateSpec through(Class<?> throughType, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId) {
		this.relationshipType = TO_MANY_THROUGH;
		this.toManyThrough = new ToManyThrough(mainType, relatedType, results);
		toManyThrough.through(throughType, fkPropertyToMainObjId, fkPropertyToRelatedObjId);
		return this;
	}

	public GetListSpec populate(String propertyToPopulateOnMainObj) {
		if (relationshipType.equals(TO_ONE)) {
			toOne.populate(propertyToPopulateOnMainObj);
		} else if (relationshipType.equals(TO_MANY)) {
			toMany.populate(propertyToPopulateOnMainObj);
		} else {
			// toManyThrough
			toManyThrough.populate(propertyToPopulateOnMainObj);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<T> type) {
		ExtractorEntityResult result = RelationshipMapper.getExtractorEntityResult(type, results);
		return (List<T>) result.list();
	}

}
