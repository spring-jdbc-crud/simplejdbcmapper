package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTypeHandler implements TypeHandler {
	public Integer getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		Integer value = rs.getInt(columnIndex);
		return (rs.wasNull() ? null : value);
	}

}
