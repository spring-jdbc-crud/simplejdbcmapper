package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringTypeHandler implements TypeHandler {
	public String getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		return rs.getString(columnIndex);
	}

}
