package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatTypeHandler implements TypeHandler {
	public Float getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		Float value = rs.getFloat(columnIndex);
		return (rs.wasNull() ? null : value);
	}

}
