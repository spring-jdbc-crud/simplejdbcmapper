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
 * The Through specification for a toMany relationship through an intermediate
 * table.
 * 
 * @author Antony Joseph
 */
public interface ThroughSpec {
	/**
	 * The id property names whose values are used to match the corresponding
	 * property values of properties configured in the
	 * {@link io.github.simplejdbcmapper.relationship.ToManySpec#through} method
	 * (intermediate table info) of the relationship.
	 * 
	 * @param mainObjIdProperty
	 * @param relatedObjIdProperty
	 * @return PopulateSpec
	 */
	PopulateSpec ids(String mainObjIdProperty, String relatedObjIdProperty);
}
