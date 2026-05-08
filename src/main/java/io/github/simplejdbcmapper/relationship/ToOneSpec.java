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
 * Specification for the ToOne relationship
 * 
 * @author Antony Joseph
 */
public interface ToOneSpec {
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
	 * @return The PopulateSpec
	 * 
	 */
	PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty);
}
