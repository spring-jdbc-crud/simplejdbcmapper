package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteArrayTypeHandler implements TypeHandler {
	public byte[] getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		return rs.getBytes(columnIndex);
	}
}
