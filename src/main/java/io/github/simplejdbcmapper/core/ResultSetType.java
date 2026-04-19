package io.github.simplejdbcmapper.core;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;

public class ResultSetType {
	public static final int BIGDECIMAL = 1;
	public static final int BLOB = 2;
	public static final int BOOLEAN = 3;
	public static final int BYTEARRAY = 4;
	public static final int BYTE = 5;
	public static final int CLOB = 6;
	public static final int DATE = 7;
	public static final int DOUBLE = 8;
	public static final int ENUM = 9;
	public static final int FLOAT = 10;
	public static final int INTEGER = 11;
	public static final int LONG = 12;
	public static final int NUMBER = 13;
	public static final int SHORT = 14;
	public static final int STRING = 15;
	public static final int TIMESTAMP = 16;
	public static final int TIME = 17;
	public static final int UTILDATE = 18;

	public static final int UNKNOWN = -1;

	public static int getResultSetType(Class<?> type) {
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
