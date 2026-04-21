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
package io.github.simplejdbcmapper.core;

import org.springframework.util.Assert;

/**
 * This is used to generate the "ORDER BY" clause for the queries.
 * 
 * @author Antony Joseph
 */
public class SortBy {
	private final String propertyName;
	private final String direction;

	/**
	 * The constructor
	 * 
	 * @param propertyName the property name to sort by. (NOT the column name)
	 */
	public SortBy(String propertyName) {
		this(propertyName, null);
	}

	/**
	 * The constructor
	 * 
	 * @param propertyName the property name to sort by (NOT the column name)
	 * @param direction    the direction of the sort. Has to be either "ASC" or
	 *                     "DESC"
	 */
	public SortBy(String propertyName, String direction) {
		Assert.notNull(propertyName, "propertyName must not be null");
		this.propertyName = propertyName;
		if (direction != null) {
			if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) {
				throw new IllegalArgumentException("direction should be ASC or DESC");
			}
			this.direction = direction.toUpperCase();
		} else {
			this.direction = "";
		}
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getDirection() {
		return direction;
	}
}
