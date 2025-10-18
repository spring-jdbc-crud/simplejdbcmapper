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

import java.util.Locale;

import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.StringUtils;

/**
 * Utility methods used by mapper.
 *
 * @author Antony Joseph
 */
class InternalUtils {

	private InternalUtils() {
	}

	/**
	 * Converts underscore case to camel case. Ex: user_last_name gets converted to
	 * userLastName.
	 *
	 * @param str underscore case string
	 * @return the camel case string
	 */
	public static String toCamelCaseName(String str) {
		return JdbcUtils.convertUnderscoreNameToPropertyName(str);
	}

	/**
	 * Converts camel case to underscore case. Ex: userLastName gets converted to
	 * user_last_name. Copy of code from Spring BeanPropertyRowMapper
	 *
	 * @param str camel case string
	 * @return the underscore case string
	 */
	public static String toUnderscoreName(String str) {
		if (!StringUtils.hasText(str)) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		result.append(Character.toLowerCase(str.charAt(0)));
		for (int i = 1; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isUpperCase(c)) {
				result.append('_').append(Character.toLowerCase(c));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	public static String toLowerCase(String str) {
		return str != null ? str.toLowerCase(Locale.US) : null;
	}

}
