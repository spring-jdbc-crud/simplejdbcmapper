package io.github.simplejdbcmapper.core;

import org.springframework.util.Assert;

import io.github.simplejdbcmapper.exception.MapperException;

public class EntityWrapper {
	private TableMapping tableMapping;
	private Object object;

	public EntityWrapper(Object object, TableMapping tableMapping) {
		Assert.notNull(object, "object must not be null");
		Assert.notNull(object, "tableMapping must not be null");
		if (object.getClass() != tableMapping.getMappedObjType()) {
			throw new IllegalArgumentException("object class " + object.getClass().getSimpleName()
					+ " not same as table mappings mapped object type " + tableMapping.getMappedObjType());
		}

		this.object = object;
		this.tableMapping = tableMapping;
	}

	public Object getPropertyValue(String propertyName) {
		PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
		if (propMapping == null) {
			throw new IllegalArgumentException(
					propertyName + " is not a mapped property for " + object.getClass().getName());
		}
		return getPropertyValue(propMapping);
	}

	public void setPropertyValue(String propertyName, Object val) {
		PropertyMapping propMapping = tableMapping.getPropertyMappingByPropertyName(propertyName);
		if (propMapping == null) {
			throw new IllegalArgumentException(
					propertyName + " is not a mapped property for " + object.getClass().getName());
		}
		setPropertyValue(propMapping, val);
	}

	public Object getIdPropertyValue() {
		PropertyMapping propMapping = tableMapping.getIdPropertyMapping();
		return getPropertyValue(propMapping);
	}

	public Object getVersionPropertyValue() {
		PropertyMapping propMapping = tableMapping.getVersionPropertyMapping();
		return propMapping == null ? null : getPropertyValue(propMapping);
	}

	public PropertyMapping getPropertyMapping(String propertyName) {
		return tableMapping.getPropertyMappingByPropertyName(propertyName);
	}

	public Integer getColumnSqlType(String propertyName) {
		// todo check null ?
		PropertyMapping propMapping = getPropertyMapping(propertyName);
		return propMapping.getColumnSqlType();
	}

	public String getVersionPropertyName() {
		return tableMapping.getVersionPropertyMapping().getPropertyName();
	}

	public PropertyMapping getUpdatedByPropertyMapping() {
		return tableMapping.getUpdatedByPropertyMapping();
	}

	public PropertyMapping getUpdatedOnPropertyMapping() {
		return tableMapping.getUpdatedOnPropertyMapping();
	}

	public boolean hasAutoAssignProperties() {
		return tableMapping.hasAutoAssignProperties();
	}

	public Class<?> getWrappedClass() {
		return object.getClass();
	}

	private Object getPropertyValue(PropertyMapping propMapping) {
		if (propMapping != null) {
			try {
				return propMapping.getReadMethod().invoke(object);
			} catch (Exception e) {
				throw new MapperException(e.getMessage() + " error while trying to get value of "
						+ object.getClass().getSimpleName() + "." + propMapping.getPropertyName());
			}
		}
		return null;
	}

	private Object setPropertyValue(PropertyMapping propMapping, Object val) {
		if (propMapping != null) {
			try {
				return propMapping.getWriteMethod().invoke(object, val);
			} catch (Exception e) {
				throw new MapperException(e.getMessage() + " error while trying to set value of "
						+ object.getClass().getSimpleName() + "." + propMapping.getPropertyName());
			}
		}
		return null;
	}

}
