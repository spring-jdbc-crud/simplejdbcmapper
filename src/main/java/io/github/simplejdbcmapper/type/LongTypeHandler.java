package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LongTypeHandler implements TypeHandler {
	public Long getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		Long value = rs.getLong(columnIndex);
		return (rs.wasNull() ? null : value);
	}

}