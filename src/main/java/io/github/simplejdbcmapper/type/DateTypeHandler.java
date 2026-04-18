package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DateTypeHandler implements TypeHandler {
	public java.sql.Date getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		return rs.getDate(columnIndex);
	}
}
