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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Some utility methods for relationships
 * 
 * @author antonyjoseph
 */
class RelationshipUtils {
	static final String TO_MANY = "toMany";
	static final String TO_ONE = "toOne";
	static final String TO_MANY_THROUGH = "toManyThrough";

	private static final String IS_PREFIX = "is";
	private static final String GET_PREFIX = "get";
	private static final String SET_PREFIX = "set";

	static Method getReadMethod(Class<?> type, String propertyName) {
		Method m = ReflectionUtils.findMethod(type, GET_PREFIX + StringUtils.capitalize(propertyName));
		if (m == null) {
			m = ReflectionUtils.findMethod(type, IS_PREFIX + StringUtils.capitalize(propertyName));
		}
		if (m == null) {
			throw new IllegalArgumentException(
					"Invalid argument. Could not find getter for " + type.getName() + "." + propertyName);
		}
		// turn off jvm access verification for invoke()
		m.trySetAccessible();
		return m;
	}

	static Method getWriteMethod(Class<?> type, String propertyName) {
		Class<?> propertyType = getPropertyType(type, propertyName);
		Method m = ReflectionUtils.findMethod(type, SET_PREFIX + StringUtils.capitalize(propertyName), propertyType);
		if (m == null) {
			throw new IllegalArgumentException(
					"Invalid argument. Could not find setter for " + type.getName() + "." + propertyName);
		}
		// turn off jvm access verification for invoke()
		m.trySetAccessible();
		return m;
	}

	static Class<?> getPropertyType(Class<?> type, String propertyName) {
		Field field = ReflectionUtils.findField(type, propertyName);
		if (field != null) {
			return field.getType();
		} else {
			throw new IllegalArgumentException(
					"Invalid argument. Property name " + propertyName + " does not exist for " + type.getName());
		}
	}

	private RelationshipUtils() {
	}

}
