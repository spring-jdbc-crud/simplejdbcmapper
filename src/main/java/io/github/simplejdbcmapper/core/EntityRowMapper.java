package io.github.simplejdbcmapper.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * A lighter row mapper than Spring's BeanPropertyRowMapper since column to
 * property relationship is already available through TableMapping.
 * 
 * @param <T> the entityType
 */
class EntityRowMapper<T> implements RowMapper<T> {
	private Class<T> mappedClass;
	private TableMapping tableMapping;
	private ConversionService conversionService;

	public EntityRowMapper(Class<T> type, TableMapping tableMapping, ConversionService conversionService) {
		this.mappedClass = type;
		this.tableMapping = tableMapping;
		this.conversionService = conversionService;
	}

	@Override
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		T obj = null;
		try {
			obj = mappedClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Could not instantiate class " + mappedClass.getName(), e);
		}
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(obj);
		bw.setConversionService(conversionService);

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		for (int index = 1; index <= columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index);
			column = InternalUtils.toLowerCase(column);
			PropertyMapping propMapping = tableMapping.getPropertyMappingByColumnName(column);
			if (propMapping != null) {
				try {
					Object value = JdbcUtils.getResultSetValue(rs, index,
							bw.getPropertyType(propMapping.getPropertyName()));
					bw.setPropertyValue(propMapping.getPropertyName(), value);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		return obj;
	}
}
