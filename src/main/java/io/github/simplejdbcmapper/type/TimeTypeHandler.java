package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TimeTypeHandler implements TypeHandler {
	public java.sql.Time getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		return rs.getTime(columnIndex);
	}
}
