package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShortTypeHandler implements TypeHandler {
	public Short getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		Short value = rs.getShort(columnIndex);
		return (rs.wasNull() ? null : value);
	}
}