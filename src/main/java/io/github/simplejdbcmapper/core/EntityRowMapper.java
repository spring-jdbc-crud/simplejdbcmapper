package io.github.simplejdbcmapper.core;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.NumberUtils;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * A lighter row mapper than Spring's BeanPropertyRowMapper since column to
 * property relationship is already available through TableMapping and avoids
 * conversion it it can.
 * 
 * @param <T> the entityType
 */
class EntityRowMapper<T> implements RowMapper<T> {
	private Class<T> mappedClass;
	private TableMapping tableMapping;
	private ConversionService conversionService;
	private boolean resultSetTyped = true;

	public EntityRowMapper(Class<T> entityType, TableMapping tableMapping, ConversionService conversionService) {
		this.mappedClass = entityType;
		this.tableMapping = tableMapping;
		this.conversionService = conversionService;
	}

	@Override
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		T obj = null;
		try {
			obj = mappedClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new MapperException("Could not instantiate class " + mappedClass.getName(), e);
		}
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(obj);
		bw.setConversionService(conversionService);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		for (int index = 1; index <= columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index);
			column = InternalUtils.toLowerCase(column);
			PropertyMapping propMapping = tableMapping.getPropertyMappingByColumnName(column);
			if (propMapping != null) {
				try {
					// bw.setPropertyValue() has to go through the conversion process and some other
					// logic. Avoid it if we can
					Object value = getResultSetValue(rs, index, propMapping.getPropertyType());
					if (resultSetTyped || value == null) {
						PropertyDescriptor pd = bw.getPropertyDescriptor(propMapping.getPropertyName());
						Method writeMethod = pd.getWriteMethod();
						writeMethod.invoke(obj, value);
					} else {
						// getResultSetValue() could not extract a typed value so using bean wrapper to
						// go through the conversion process
						bw.setPropertyValue(propMapping.getPropertyName(), value);
					}
				} catch (Exception ex) {
					throw new MapperException(ex);
				}
			}
		}
		return obj;
	}

	/*
	 * Copy of Springs JdbcUtils.getResultSetValue(). The difference is it sets
	 * resultSetTyped flag if it can explicitly extract typed value
	 */
	public Object getResultSetValue(ResultSet rs, int index, Class<?> requiredType) throws SQLException {
		resultSetTyped = true;
		// Explicitly extract typed value, as far as possible.
		if (String.class == requiredType) {
			return rs.getString(index);
		} else if (Boolean.class == requiredType) {
			return rs.getBoolean(index);
		} else if (Byte.class == requiredType) {
			return rs.getByte(index);
		} else if (Short.class == requiredType) {
			return rs.getShort(index);
		} else if (Integer.class == requiredType) {
			return rs.getInt(index);
		} else if (Long.class == requiredType) {
			return rs.getLong(index);
		} else if (Float.class == requiredType) {
			return rs.getFloat(index);
		} else if (Double.class == requiredType || Number.class == requiredType) {
			return rs.getDouble(index);
		} else if (BigDecimal.class == requiredType) {
			return rs.getBigDecimal(index);
		} else if (java.sql.Date.class == requiredType) {
			return rs.getDate(index);
		} else if (java.sql.Time.class == requiredType) {
			return rs.getTime(index);
		} else if (java.sql.Timestamp.class == requiredType || java.util.Date.class == requiredType) {
			return rs.getTimestamp(index);
		} else if (byte[].class == requiredType) {
			return rs.getBytes(index);
		} else if (Blob.class == requiredType) {
			return rs.getBlob(index);
		} else if (Clob.class == requiredType) {
			return rs.getClob(index);
		} else if (requiredType.isEnum()) {
			resultSetTyped = false;
			// Enums can either be represented through a String or an enum index value:
			// leave enum type conversion up to the caller (for example, a
			// ConversionService)
			// but make sure that we return nothing other than a String or an Integer.
			Object obj = rs.getObject(index);
			if (obj instanceof String) {
				return obj;
			} else if (obj instanceof Number number) {
				// Defensively convert any Number to an Integer (as needed by our
				// ConversionService's IntegerToEnumConverterFactory) for use as index
				return NumberUtils.convertNumberToTargetClass(number, Integer.class);
			} else {
				// for example, on Postgres: getObject returns a PGObject, but we need a String
				return rs.getString(index);
			}
		} else {
			resultSetTyped = false;
			// Some unknown type desired -> rely on getObject.
			try {
				return rs.getObject(index, requiredType);
			} catch (Exception ex) {
				// jdbc driver has no support for this.
			}

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
	}

}
