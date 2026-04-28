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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Locale;

import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.support.SqlBinaryValue;
import org.springframework.jdbc.core.support.SqlCharacterValue;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.StringUtils;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * Utility methods used by mapper.
 *
 * @author Antony Joseph
 */
class InternalUtils {

	/*
	 * Same logic as Springs JdbcUtil.getResultSetValue().
	 * JdbcUtil.getResultSetValue() logic has been proven over the years, retaining
	 * its logic but changed the structure to use 'switch' statement with enums
	 * instead of the bunch of if/else's for performance reasons. As was the goal,
	 * java compiled the switch statement into a 'tableswitch' which means the
	 * program will jump directly to the correct 'case' block in one step.
	 */
	public static Object getResultSetValue(ResultSet rs, int index, ResultSetType resultSetType, Class<?> requiredType,
			boolean[] typedValueExtracted) throws SQLException {
		typedValueExtracted[0] = true;
		Object value;
		// Explicitly extract typed value, as far as possible.
		switch (resultSetType) {
		case ResultSetType.STRING:
			return rs.getString(index);
		case ResultSetType.BOOLEAN:
			value = rs.getBoolean(index);
			break;
		case ResultSetType.BYTE:
			value = rs.getByte(index);
			break;
		case ResultSetType.SHORT:
			value = rs.getShort(index);
			break;
		case ResultSetType.INTEGER:
			value = rs.getInt(index);
			break;
		case ResultSetType.LONG:
			value = rs.getLong(index);
			break;
		case ResultSetType.FLOAT:
			value = rs.getFloat(index);
			break;
		case ResultSetType.DOUBLE:
			value = rs.getDouble(index);
			break;
		case ResultSetType.NUMBER: // same as double
			value = rs.getDouble(index);
			break;
		case ResultSetType.BIGDECIMAL:
			return rs.getBigDecimal(index);
		case ResultSetType.DATE:
			return rs.getDate(index);
		case ResultSetType.TIME:
			return rs.getTime(index);
		case ResultSetType.TIMESTAMP:
			return rs.getTimestamp(index);
		case ResultSetType.UTILDATE: // java.util.Date. same as timestamp
			return rs.getTimestamp(index);
		case ResultSetType.BYTEARRAY:
			return rs.getBytes(index);
		case ResultSetType.BLOB:
			return rs.getBlob(index);
		case ResultSetType.CLOB:
			return rs.getClob(index);
		case ResultSetType.ENUM:
			typedValueExtracted[0] = false;
			// Enums are represented as a String in simpleJdbcMapper.
			// leave enum type conversion up to the caller (for example, a
			// ConversionService)
			// but make sure that we return nothing other than a String
			Object obj = rs.getObject(index);
			if (obj instanceof String) {
				return obj;
			} else {
				// for example, on Postgres: getObject returns a PGObject, but we need a String
				return rs.getString(index);
			}
		default:
			// Some unknown type desired -> rely on getObject.
			try {
				return rs.getObject(index, requiredType);
			} catch (Exception ex) {
				// jdbc driver does not support
			}
			typedValueExtracted[0] = false;

			// Corresponding SQL types for JSR-310, left up to the caller to convert
			// them (for example, through a ConversionService).
			String typeName = requiredType.getSimpleName();
			return switch (typeName) {
			case "LocalDate" -> rs.getDate(index);
			case "LocalTime" -> rs.getTime(index);
			case "LocalDateTime" -> rs.getTimestamp(index);
			// Fall back to getObject without type specification, again
			// left up to the caller to convert the value if necessary.
			default -> JdbcUtils.getResultSetValue(rs, index);
			};

		}
		// Perform was-null check if necessary (for results that the JDBC driver returns
		// as primitives).
		return (rs.wasNull() ? null : value);
	}

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

	private InternalUtils() {
	}
}
