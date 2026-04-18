package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTypeHandler implements TypeHandler {
	public Double getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		Double value = rs.getDouble(columnIndex);
		return (rs.wasNull() ? null : value);
	}

}
