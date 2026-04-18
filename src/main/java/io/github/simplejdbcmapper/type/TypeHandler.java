package io.github.simplejdbcmapper.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeHandler {
	Object getValue(ResultSet rs, int columnIndex, Class<?> type) throws SQLException;
}
