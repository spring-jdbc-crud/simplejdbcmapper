package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EnumTypeHandler implements TypeHandler {
	public String getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		// Enums can either be represented through a String
		// leave enum type conversion up to the caller (for example, a
		// ConversionService)
		// but make sure that we return nothing other than a String or an Integer.
		Object obj = rs.getObject(columnIndex);
		if (obj instanceof String) {
			return (String) obj;
		} else {
			// for example, on Postgres: getObject returns a PGObject, but we need a String
			return rs.getString(columnIndex);
		}
	}
}
