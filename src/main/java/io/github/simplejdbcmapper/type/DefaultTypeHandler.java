package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.support.JdbcUtils;

/**
 * The logic is from Spring's JdbcUtils.getResultSetValue(). Just copied it
 * over.
 */
public class DefaultTypeHandler implements TypeHandler {

	public Object getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		// Some unknown type desired -> rely on getObject.
		try {
			return rs.getObject(columnIndex, requiredType);
		} catch (Exception ex) {
			// jdbc driver does not support this.
		}
		// Corresponding SQL types for JSR-310, left up to the caller to convert
		// them (for example, through a ConversionService).
		String typeName = requiredType.getSimpleName();
		return switch (typeName) {
		case "LocalDate" -> rs.getDate(columnIndex);
		case "LocalTime" -> rs.getTime(columnIndex);
		case "LocalDateTime" -> rs.getTimestamp(columnIndex);
		// Fall back to getObject without type specification, again
		// left up to the caller to convert the value if necessary.
		default -> JdbcUtils.getResultSetValue(rs, columnIndex);
		};
	}
}