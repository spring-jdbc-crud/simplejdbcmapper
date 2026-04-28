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

import java.sql.Types;
import java.util.Locale;

import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.support.SqlBinaryValue;
import org.springframework.jdbc.core.support.SqlCharacterValue;
import org.springframework.util.StringUtils;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * Utility methods used by mapper.
 *
 * @author Antony Joseph
 */
class InternalUtils {

	public static Integer javaTypeToSqlParameterType(Class<?> entityType) {
		if (entityType.isEnum()) {
			return Types.VARCHAR;
		} else {
			// use springs default mappings.
			return StatementCreatorUtils.javaTypeToSqlParameterType(entityType);
		}
	}

	public static void assignBlobMapSqlParameterSource(BeanWrapper bw, MapSqlParameterSource mapSqlParameterSource,
			PropertyMapping propMapping, Integer columnSqlType, boolean forInsert) {
		Object val = bw.getPropertyValue(propMapping.getPropertyName());
		String param = propMapping.getPropertyName();
		if (forInsert) {
			// for inserts parameters are column name
			param = propMapping.getColumnName();
		}
		if (val == null) {
			mapSqlParameterSource.addValue(param, null, columnSqlType);
		} else if (val instanceof byte[] byteArray) {
			mapSqlParameterSource.addValue(param, new SqlBinaryValue(byteArray), columnSqlType);
		} else {
			throw new MapperException(bw.getWrappedClass().getSimpleName() + "." + propMapping.getPropertyName()
					+ " : java type has to be byte[] for a BLOB mapping. No other type is supported.");
		}
	}

	public static void assignClobMapSqlParameterSource(BeanWrapper bw, MapSqlParameterSource mapSqlParameterSource,
			PropertyMapping propMapping, Integer columnSqlType, boolean forInsert) {
		Object val = bw.getPropertyValue(propMapping.getPropertyName());
		String param = propMapping.getPropertyName();
		if (forInsert) {
			// for inserts parameters are column name
			param = propMapping.getColumnName();
		}
		if (val == null) {
			mapSqlParameterSource.addValue(param, null, columnSqlType);
		} else {
			if (val instanceof String str) {
				mapSqlParameterSource.addValue(param, new SqlCharacterValue(str), columnSqlType);
			} else {
				throw new MapperException(bw.getWrappedClass().getSimpleName() + "." + propMapping.getPropertyName()
						+ " : java type has to be String for a CLOB mapping. No other type is supported");
			}
		}
	}

	public static void assignEnumMapSqlParameterSource(BeanWrapper bw, MapSqlParameterSource mapSqlParameterSource,
			PropertyMapping propMapping, Integer columnSqlType, boolean forInsert) {
		Object enumObj = bw.getPropertyValue(propMapping.getPropertyName());
		String param = propMapping.getPropertyName();
		if (forInsert) {
			param = propMapping.getColumnName();
		}
		if (enumObj != null) {
			mapSqlParameterSource.addValue(param, ((Enum<?>) enumObj).name(), columnSqlType);
		} else {
			mapSqlParameterSource.addValue(param, null, columnSqlType);
		}
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
		if (!StringUtils.hasLength(str)) {
			return "";
		}
		return str.toLowerCase(Locale.US);
	}

	public static boolean isAlphanumeric(String str) {
		return str != null && str.matches("^[a-zA-Z0-9]*$");
	}

	public static void validateTableAlias(String tableAlias) {
		if (!StringUtils.hasText(tableAlias)) {
			throw new IllegalArgumentException("tableAlias has no value");
		}
		if (InternalUtils.isAlphanumeric(tableAlias)) {
			if (!Character.isLetter(tableAlias.charAt(0))) {
				throw new IllegalArgumentException("tableAlias should start with an alphabet.");
			}
		} else {
			throw new IllegalArgumentException("tableAlias should be alphanumberic.");
		}
	}

	private InternalUtils() {
	}
}
