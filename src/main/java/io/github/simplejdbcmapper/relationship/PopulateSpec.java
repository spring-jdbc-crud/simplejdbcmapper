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
 * @deprecated The populate specification.
 * 
 * @author Antony Joseph
 */
@Deprecated(since = "2.4.0", forRemoval = true)
public interface PopulateSpec {
	/**
	 * Processes the relationship and populates the target property.
	 * 
	 * <p>
	 * For a toMany relationship the target property <b>always has to be a List</b>.
	 * 
	 * @param propertyToPopulateOnMainObj the target property on the main object
	 *                                    which needs to be populated.
	 */
	GetListSpec populate(String propertyToPopulateOnMainObj);
}
