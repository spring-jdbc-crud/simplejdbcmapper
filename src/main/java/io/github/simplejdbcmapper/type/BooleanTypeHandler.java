package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanTypeHandler implements TypeHandler {

	public Boolean getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		Boolean value = rs.getBoolean(columnIndex);
		return (rs.wasNull() ? null : value);
	}

}
