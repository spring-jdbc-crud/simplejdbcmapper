package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ByteTypeHandler implements TypeHandler {

	public Byte getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		Byte value = rs.getByte(columnIndex);
		return (rs.wasNull() ? null : value);
	}

}
