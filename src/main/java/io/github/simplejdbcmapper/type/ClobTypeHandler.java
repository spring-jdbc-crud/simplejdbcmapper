package io.github.simplejdbcmapper.type;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClobTypeHandler implements TypeHandler {
	public Clob getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		return rs.getClob(columnIndex);
	}
}
