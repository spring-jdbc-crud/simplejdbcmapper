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

/**
 * The relationship fluent interface.
 */
public interface RelationshipFluent {
	/**
	 * The relationship type interface.
	 */
	interface RelationshipType {
		/**
		 * A toOne relationship.
		 * 
		 * @param relatedType the related type
		 * 
		 * @return ToOne
		 */
		ToOne toOne(Class<?> relatedType);

		/**
		 * A toMany relationship.
		 * 
		 * @param relatedType the related type
		 * 
		 * @return ToMany
		 */
		ToMany toMany(Class<?> relatedType);

	}

	/**
	 * The toOne interface.
	 */
	public interface ToOne {
		/**
		 * The properties on the two lists whose values should be matched to populate
		 * the toOne relationship. <b>The java type of both the properties have to be
		 * exactly the same.</b>
		 * 
		 * For example one should not be Long and the other an Integer.
		 * 
		 * 
		 * @param mainObjJoinProperty    The main object property for matching
		 * @param relatedObjJoinProperty The related object property for matching
		 * @return Populate
		 * 
		 */
		Populate joinOn(String mainObjJoinProperty, String relatedObjJoinProperty);
	}

	/**
	 * The toMany interface.
	 */
	interface ToMany {
		/**
		 * The properties on each list whose values should be matched to populate the
		 * toMany relationship.
		 * 
		 * <b>The java type of both the properties have to be exactly the same.</b>
		 * 
		 * @param mainObjIdProperty    The main object id
		 * @param relatedObjFkProperty The related object foreign key used for matching
		 * @return Populate
		 */
		Populate joinOn(String mainObjIdProperty, String relatedObjFkProperty);

		/**
		 * The 'through' information when defining a toMany relationship through an
		 * intermediate table (one side of many to many).
		 * 
		 * @param throughType              The intermediate type
		 * @param fkPropertyToMainObjId    the foreign key property that matches the
		 *                                 main object's id
		 * @param fkPropertyToRelatedObjId The foreign key property that matches the
		 *                                 related object's id
		 * @return Populate
		 */
		Populate through(Class<?> throughType, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId);
	}

	/**
	 * The populate interface.
	 */
	interface Populate {
		/**
		 * The property to populate on the main object.
		 * 
		 * @param mainObjPropertyToPopulate the target property on the main object which
		 *                                  needs to be populated.
		 */
		Relationship populate(String mainObjPropertyToPopulate);
	}

}
