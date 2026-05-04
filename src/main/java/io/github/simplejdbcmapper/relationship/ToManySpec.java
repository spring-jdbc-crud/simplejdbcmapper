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
 * Specification for the ToMany relationship
 * 
 * @author Antony Joseph
 */
public interface ToManySpec {

	/**
	 * The properties on each list whose values should be matched to populate the
	 * toMany relationship.
	 * 
	 * <b>The java type of both the properties have to be exactly the same.</b>
	 * 
	 * For example one should not be Long and the other an Integer.
	 * 
	 * @param mainObjIdProperty    The main object id
	 * @param relatedObjFkProperty The related object foreign key used for matching
	 * @return The populateSpec
	 */
	PopulateSpec joinOn(String mainObjIdProperty, String relatedObjFkProperty);

	/**
	 * The 'through' information when defining a toMany relationship through an
	 * intermediate table (one side of many to many).
	 * 
	 * @param intermediateList         The intermediate table list
	 * @param fkPropertyToMainObjId    the foreign key property that matches the
	 *                                 main object's id
	 * @param fkPropertyToRelatedObjId The foreign key property that matches the
	 *                                 related object's id
	 * @return the ThroughSpec
	 */
	ThroughSpec through(List<?> intermediateList, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId);
}
