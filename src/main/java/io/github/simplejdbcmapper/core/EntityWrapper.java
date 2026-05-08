package io.github.simplejdbcmapper.core;

import org.springframework.core.convert.ConversionService;

import io.github.simplejdbcmapper.exception.MapperException;

class EntityWrapper {
	private Object object;
	private TableMapping tableMapping;

	public EntityWrapper(Object object, TableMapping tableMapping) {
		this.object = object;
		this.tableMapping = tableMapping;
	}

	public Object getPropertyValue(PropertyMapping propMapping) {
		try {
			return propMapping.getReadMethod().invoke(object);
		} catch (Exception e) {
			throw new MapperException(e.getMessage() + " error while trying to get value of "
					+ object.getClass().getSimpleName() + "." + propMapping.getPropertyName());
		}
	}

	public void setPropertyValue(PropertyMapping propMapping, Object val) {
		setPropertyValue(propMapping, val, null);
	}

	public void setPropertyValue(PropertyMapping propMapping, Object val, ConversionService conversionService) {
		try {
			if (conversionService == null) {
				propMapping.getWriteMethod().invoke(object, val);
			} else {
				propMapping.getWriteMethod().invoke(object,
						conversionService.convert(val, propMapping.getPropertyType()));
			}
		} catch (Exception e) {
			throw new MapperException(
					e.getMessage() + ". Invoking " + propMapping.getWriteMethod() + " with value " + val, e);
		}
	}

	public Class<?> getWrappedClass() {
		return object.getClass();
	}

	public Object getWrappedInstance() {
		return object;
	}

	public TableMapping getTableMapping() {
		return tableMapping;
	}

}
