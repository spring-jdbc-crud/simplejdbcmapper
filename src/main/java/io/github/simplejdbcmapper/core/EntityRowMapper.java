package io.github.simplejdbcmapper.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.NumberUtils;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * A lighter row mapper than Spring's BeanPropertyRowMapper since column to
 * property relationship is already available through TableMapping and avoids
 * conversion if it can.
 * 
 * @param <T> the entityType
 */
class EntityRowMapper<T> implements RowMapper<T> {
	private Class<T> mappedClass;
	private TableMapping tableMapping;
	private ConversionService conversionService;
	private boolean typedValueExtracted = true;

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
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		for (int index = 1; index <= columnCount; index++) {
			PropertyMapping propMapping = tableMapping.getPropertyMappingByResultSetIndex(index);
			if (propMapping != null) {
				try {
					Object value = getResultSetValue(rs, index, propMapping.getResultSetTypeEnum(),
							propMapping.getPropertyType());
					if (typedValueExtracted || value == null) {
						propMapping.getWriteMethod().invoke(obj, value);
					} else {
						try {
							System.out.println("property needs conversion:" + propMapping.getPropertyName() + " type: "
									+ propMapping.getPropertyType() + " resultSet value type: " + value.getClass());
							propMapping.getWriteMethod().invoke(obj,
									conversionService.convert(value, propMapping.getPropertyType()));
						} catch (ConverterNotFoundException cnfex) {
							throw new MapperException("For property " + mappedClass.getSimpleName() + "."
									+ propMapping.getPropertyName() + " could not convert ResulSet value of class "
									+ value.getClass() + " to type of the property " + propMapping.getPropertyType(),
									cnfex);
						}
					}
				} catch (Exception ex) {
					throw new MapperException(ex);
				}
			}
		}
		return obj;
	}

	/*
	 * Same logic as Springs JdbcUtil.getResultSetValue(). The difference is, being
	 * able to use 'switch' instead of all the 'if else's. Checked the complied java
	 * code and it has compiled the switch statement into a 'tableswitch' which
	 * means the program can jump directly to the correct case block in one step.
	 * Also set the typeValueExtracted flag which allows mapRow() to easily figure
	 * out whether the property needs conversion
	 */

	private Object getResultSetValue(ResultSet rs, int index, ResultSetTypeEnum resultSetType, Class<?> requiredType)
			throws SQLException {
		typedValueExtracted = true;
		Object value = null;
		// Explicitly extract typed value, as far as possible.
		switch (resultSetType) {
		case ResultSetTypeEnum.STRING:
			return rs.getString(index);
		case ResultSetTypeEnum.BOOLEAN:
			value = rs.getBoolean(index);
			break;
		case ResultSetTypeEnum.BYTE:
			value = rs.getByte(index);
			break;
		case ResultSetTypeEnum.SHORT:
			value = rs.getShort(index);
			break;
		case ResultSetTypeEnum.INTEGER:
			value = rs.getInt(index);
			break;
		case ResultSetTypeEnum.LONG:
			value = rs.getLong(index);
			break;
		case ResultSetTypeEnum.FLOAT:
			value = rs.getFloat(index);
			break;
		case ResultSetTypeEnum.DOUBLE:
			value = rs.getDouble(index);
			break;
		case ResultSetTypeEnum.NUMBER: // same as double
			value = rs.getDouble(index);
			break;
		case ResultSetTypeEnum.BIGDECIMAL:
			return rs.getBigDecimal(index);
		case ResultSetTypeEnum.DATE:
			return rs.getDate(index);
		case ResultSetTypeEnum.TIME:
			return rs.getTime(index);
		case ResultSetTypeEnum.TIMESTAMP:
			return rs.getTimestamp(index);
		case ResultSetTypeEnum.UTILDATE: // same as timestamp
			return rs.getTimestamp(index);
		case ResultSetTypeEnum.BYTEARRAY:
			return rs.getBytes(index);
		case ResultSetTypeEnum.BLOB:
			return rs.getBlob(index);
		case ResultSetTypeEnum.CLOB:
			return rs.getClob(index);
		case ResultSetTypeEnum.ENUM:
			typedValueExtracted = false;
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
		default:
			// Some unknown type desired -> rely on getObject.
			try {
				return rs.getObject(index, requiredType);
			} catch (Exception ex) {
				// jdbc driver does not support
			}
			typedValueExtracted = false;

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

}