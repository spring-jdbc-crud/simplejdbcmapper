package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TimestampTypeHandler implements TypeHandler {
	public java.sql.Timestamp getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		return rs.getTimestamp(columnIndex);
	}
}
