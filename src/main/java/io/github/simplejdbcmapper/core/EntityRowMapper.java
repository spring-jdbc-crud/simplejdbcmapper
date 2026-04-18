package io.github.simplejdbcmapper.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * A lighter row mapper than Spring's BeanPropertyRowMapper since column to
 * property relationship is already available through TableMapping and avoids
 * conversion if it can.
 * 
 * @param <T> the entityType
 */
class EntityRowMapper<T> implements RowMapper<T> {
	private Class<T> mappedClass;
	private TableMapping tableMapping;
	private ConversionService conversionService;

	public EntityRowMapper(Class<T> entityType, TableMapping tableMapping, ConversionService conversionService) {
		this.mappedClass = entityType;
		this.tableMapping = tableMapping;
		this.conversionService = conversionService;
	}

	@Override
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		T obj = null;
		try {
			obj = mappedClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new MapperException("Could not instantiate class " + mappedClass.getName(), e);
		}
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		int total = 0;
		for (int index = 1; index <= columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index);
			column = InternalUtils.toLowerCase(column);

			PropertyMapping propMapping = tableMapping.getPropertyMappingByColumnName(column);
			if (propMapping != null) {
				try {
					long s1 = System.nanoTime();
					Object value = propMapping.getTypeHandler().getValue(rs, index, propMapping.getPropertyType());
					// Object value = JdbcUtils.getResultSetValue(rs, index,
					// propMapping.getPropertyType());
					long e1 = System.nanoTime();
					// System.out.println("with type handler: " + (e1 - s1) + " ns propertyName: "
					// + propMapping.getPropertyName() + " type:" + propMapping.getPropertyType());

					total += e1 - s1;
					if (value == null || propMapping.getPropertyType().isAssignableFrom(value.getClass())) {
						propMapping.getWriteMethod().invoke(obj, value);
					} else {
						try {
							propMapping.getWriteMethod().invoke(obj,
									conversionService.convert(value, propMapping.getPropertyType()));
						} catch (ConverterNotFoundException cnfex) {
							throw new MapperException("For property " + mappedClass.getSimpleName() + "."
									+ propMapping.getPropertyName() + " could not convert ResulSet value of class "
									+ value.getClass() + " to type of the property " + propMapping.getPropertyType(),
									cnfex);
						}
					}
				} catch (Exception ex) {
					throw new MapperException(ex);
				}
			}
		}
		System.out.println("Total time: " + total);
		return obj;
	}

}