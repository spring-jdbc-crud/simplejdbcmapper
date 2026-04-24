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

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;

/**
 * This enum is there because we want the switch statements in the
 * EntityRowMapper to be compiled into a 'tableswitch' by java (which it does).
 */
enum ResultSetType {

	UNKNOWN, BIGDECIMAL, BLOB, BOOLEAN, BYTEARRAY, BYTE, CLOB, DATE, DOUBLE, ENUM, FLOAT, INTEGER, LONG, NUMBER, SHORT,
	STRING, TIMESTAMP, TIME, UTILDATE;

	public static ResultSetType getResultSetType(Class<?> type) {
		if (BigDecimal.class == type) {
			return BIGDECIMAL;
		} else if (Blob.class == type) {
			return BLOB;
		} else if (Boolean.class == type) {
			return BOOLEAN;
		} else if (byte[].class == type) {
			return BYTEARRAY;
		} else if (Byte.class == type) {
			return BYTE;
		} else if (Clob.class == type) {
			return CLOB;
		} else if (java.sql.Date.class == type) {
			return DATE;
		} else if (Double.class == type) {
			return DOUBLE;
		} else if (type.isEnum()) {
			return ENUM;
		} else if (Float.class == type) {
			return FLOAT;
		} else if (Integer.class == type) {
			return INTEGER;
		} else if (Long.class == type) {
			return LONG;
		} else if (Number.class == type) {
			return NUMBER;
		} else if (Short.class == type) {
			return SHORT;
		} else if (String.class == type) {
			return STRING;
		} else if (java.sql.Timestamp.class == type) {
			return TIMESTAMP;
		} else if (java.sql.Time.class == type) {
			return TIME;
		} else if (java.util.Date.class == type) {
			return UTILDATE;
		}
		return UNKNOWN;
	}

}
