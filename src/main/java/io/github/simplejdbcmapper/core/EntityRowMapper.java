package io.github.simplejdbcmapper.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * We get the resultset values using a switch statement with enums which java
 * compiles into a 'tableswitch'. This means that the program will jump directly
 * to the correct case block in the switch statement in one step. When property
 * mappings for an entity is initialized, the property's resultSetType is
 * assigned for direct access when processing the query ResultSet. Also the
 * writeMethod (to set values by reflection) is assigned to the property mapping
 * so it is directly available.
 * 
 * <p>
 * A new instance should be created for use each time
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
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int index = 1; index <= columnCount; index++) {
				PropertyMapping propMapping = tableMapping.getPropertyMappingByResultSetIndex(index);
				Object value = getResultSetValue(rs, index, propMapping.getResultSetType(),
						propMapping.getPropertyType());
				if (typedValueExtracted || value == null) {
					propMapping.getWriteMethod().invoke(obj, value);
				} else {
					propMapping.getWriteMethod().invoke(obj,
							conversionService.convert(value, propMapping.getPropertyType()));
				}
			}
		} catch (Exception e) {
			throw new MapperException(e.getMessage(), e);
		}
		return obj;
	}

	/*
	 * Same logic as Springs JdbcUtil.getResultSetValue(). Using switch with enums
	 * instead of all the 'if else's. Checked the complied java code and it has
	 * compiled the switch statement into a 'tableswitch' which means the program
	 * can jump directly to the correct case block in one step. Also set the
	 * typeValueExtracted flag which allows mapRow() to easily figure out whether
	 * the property needs conversion
	 */
	private Object getResultSetValue(ResultSet rs, int index, ResultSetType resultSetType, Class<?> requiredType)
			throws SQLException {
		typedValueExtracted = true;
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
			typedValueExtracted = false;
			// Enums can either be represented through a String or an enum index value:
			// leave enum type conversion up to the caller (for example, a
			// ConversionService)
			// but make sure that we return nothing other than a String or an Integer.
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