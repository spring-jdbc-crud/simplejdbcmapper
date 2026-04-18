package io.github.simplejdbcmapper.type;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlobTypeHandler implements TypeHandler {
	public Blob getValue(ResultSet rs, int columnIndex, Class<?> type) throws SQLException {
		return rs.getBlob(columnIndex);
	}
}
