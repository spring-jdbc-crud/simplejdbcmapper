package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UtilDateTypeHandler implements TypeHandler {
	public java.util.Date getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		return rs.getTimestamp(columnIndex);
	}
}
