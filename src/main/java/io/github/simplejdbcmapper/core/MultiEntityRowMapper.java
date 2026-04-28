package io.github.simplejdbcmapper.core;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;

import org.springframework.core.convert.ConversionService;

import io.github.simplejdbcmapper.exception.MapperException;

final class MultiEntityRowMapper<T> {
	private final ConversionService conversionService;
	private final PropertyMapping[] propertyMappings;
	private final Constructor<T> mappedObjConstructor;
	private final int startIndex;
	private final int endIndex;

	@SuppressWarnings("unchecked")
	MultiEntityRowMapper(TableMapping tableMapping, ConversionService conversionService, int offset) {
		this.conversionService = conversionService;
		this.propertyMappings = tableMapping.getPropertyMappings();
		this.mappedObjConstructor = tableMapping.getMappedObjConstructor();
		if (offset == 1) {
			this.startIndex = 1;
			this.endIndex = propertyMappings.length;
		} else {
			this.startIndex = offset;
			this.endIndex = propertyMappings.length + offset - 1;
		}
	}

	public T mapRow(ResultSet rs) {
		T obj = null;
		try {
			obj = mappedObjConstructor.newInstance();
			boolean[] typedValueExtracted = { true };
			// since the columns sql was generated using the property mappings the resultset
			// columns will be in same order.
			// resultset indexes start at 1

			System.out.println("mapRow: MultiEntityRowMapper: " + this);

			int cnt = 0;
			for (int index = startIndex; index <= endIndex; index++) {
				System.out.println("index: " + index);
				PropertyMapping propMapping = propertyMappings[cnt];

				// System.out.println(
				// "offset: " + offset + " index: " + index + " propertyName: " +
				// propMapping.getPropertyName());
				Object value = InternalUtils.getResultSetValue(rs, index, propMapping.getResultSetType(),
						propMapping.getPropertyType(), typedValueExtracted);
				if (typedValueExtracted[0] || value == null) {
					propMapping.getWriteMethod().invoke(obj, value);
				} else {
					propMapping.getWriteMethod().invoke(obj,
							conversionService.convert(value, propMapping.getPropertyType()));
				}
				cnt++;
			}
		} catch (Exception e) {
			throw new MapperException(e.getMessage(), e);
		}
		return obj;
	}
}
