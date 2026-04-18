package io.github.simplejdbcmapper.type;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigDecimalTypeHandler implements TypeHandler {
	public BigDecimal getValue(ResultSet rs, int columnIndex, Class<?> requiredType) throws SQLException {
		return rs.getBigDecimal(columnIndex);
	}
}
