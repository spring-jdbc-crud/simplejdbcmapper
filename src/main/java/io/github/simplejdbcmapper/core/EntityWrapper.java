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

	public Object getPropertyValue(String propertyName) {
		PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
		if (propMapping == null) {
			throw new IllegalArgumentException(
					propertyName + " is not a mapped property for " + object.getClass().getName());
		}
		try {
			return propMapping.getReadMethod().invoke(object);
		} catch (Exception e) {
			throw new MapperException(e.getMessage() + " error while trying to get value of "
					+ object.getClass().getSimpleName() + "." + propMapping.getPropertyName());
		}
	}

	public void setPropertyValue(String propertyName, Object val) {
		setPropertyValue(propertyName, val, null);
	}

	public void setPropertyValue(String propertyName, Object val, ConversionService conversionService) {
		PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
		if (propMapping == null) {
			throw new IllegalArgumentException(
					propertyName + " is not a mapped property for " + object.getClass().getName());
		}
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
