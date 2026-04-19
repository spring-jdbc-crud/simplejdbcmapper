package io.github.simplejdbcmapper.core;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.exception.AnnotationException;

class TableMappingProvider {
	private final String schemaName;

	private final String catalogName;

	// Map key - class name
	// value - the table mapping
	private final SimpleCache<String, TableMapping> tableMappingCache = new SimpleCache<>();

	private final AnnotationProcessor ap;

	public TableMappingProvider(String schemaName, String catalogName) {
		this.schemaName = schemaName;
		this.catalogName = catalogName;
		this.ap = new AnnotationProcessor();
	}

	TableMapping getTableMapping(Class<?> entityType) {
		Assert.notNull(entityType, "entityType must not be null");
		TableMapping tableMapping = tableMappingCache.get(entityType.getName());
		if (tableMapping == null) {
			Table tableAnnotation = ap.getTableAnnotation(entityType);
			String tableName = tableAnnotation.name();
			String catalog = getCatalogForTable(tableAnnotation);
			String schema = getSchemaForTable(tableAnnotation);
			List<Field> fields = getAllFields(entityType);
			IdPropertyInfo idPropertyInfo = getIdPropertyInfo(entityType, fields);
			List<PropertyMapping> propertyMappings = getPropertyMappings(entityType, fields);
			tableMapping = new TableMapping(entityType, tableName, schema, catalog, idPropertyInfo, propertyMappings);
			tableMappingCache.put(entityType.getName(), tableMapping);
		}
		return tableMapping;
	}

	SimpleCache<String, TableMapping> getTableMappingCache() {
		return tableMappingCache;
	}

	private List<PropertyMapping> getPropertyMappings(Class<?> entityType, List<Field> fields) {
		// key:propertyName, value:PropertyMapping. LinkedHashMap to maintain order of
		// properties
		Map<String, PropertyMapping> propNameToPropertyMapping = new LinkedHashMap<>();
		for (Field field : fields) {
			// process column annotation always first
			ap.processColumnAnnotation(field, propNameToPropertyMapping);
			ap.processIdAnnotation(field, propNameToPropertyMapping);
			ap.processVersionAnnotation(field, propNameToPropertyMapping);
			ap.processCreatedOnAnnotation(field, propNameToPropertyMapping);
			ap.processUpdatedOnAnnotation(field, propNameToPropertyMapping);
			ap.processCreatedByAnnotation(field, propNameToPropertyMapping);
			ap.processUpdatedByAnnotation(field, propNameToPropertyMapping);
		}
		List<PropertyMapping> propertyMappings = new ArrayList<>(propNameToPropertyMapping.values());
		ap.validateAnnotations(propertyMappings, entityType);
		assignReflectionWriteMethods(entityType, propertyMappings);
		assignResultSetType(propertyMappings);
		assignResultSetTypeEnum(propertyMappings);
		return propertyMappings;
	}

	private List<Field> getAllFields(Class<?> entityType) {
		List<Field> fields = new ArrayList<>();
		Class<?> clazz = entityType;
		while (clazz != null && clazz != Object.class) {
			Collections.addAll(fields, clazz.getDeclaredFields());
			clazz = clazz.getSuperclass();
		}
		// there could be duplicate fields due to super classes. Get unique fields list
		// by name
		Set<String> set = new HashSet<>();
		return fields.stream().filter(p -> set.add(p.getName())).toList();
	}

	private void assignReflectionWriteMethods(Class<?> entityType, List<PropertyMapping> propertyMappings) {
		BeanWrapperImpl bw = new BeanWrapperImpl(entityType);
		for (PropertyMapping propMapping : propertyMappings) {
			PropertyDescriptor pd = bw.getPropertyDescriptor(propMapping.getPropertyName());
			Method writeMethod = pd.getWriteMethod();
			writeMethod.setAccessible(true);
			propMapping.setWriteMethod(writeMethod);
		}
	}

	private void assignResultSetType(List<PropertyMapping> propertyMappings) {
		for (PropertyMapping propMapping : propertyMappings) {
			int resultSetType = ResultSetType.getResultSetType(propMapping.getPropertyType());
			// System.out.println("property:" + propMapping.getPropertyName() + " type: " +
			// propMapping.getPropertyType()
			// + " resultSetType: " + resultSetType);
			propMapping.setResultSetType(resultSetType);
		}
	}

	private void assignResultSetTypeEnum(List<PropertyMapping> propertyMappings) {

		for (PropertyMapping propMapping : propertyMappings) {
			ResultSetTypeEnum val = ResultSetTypeEnum.getResultSetType(propMapping.getPropertyType());
			// System.out.println("property:" + propMapping.getPropertyName() + " type: " +
			// propMapping.getPropertyType()
			// + " resultSetType: " + resultSetType);
			propMapping.setResultSetTypeEnum(val);
		}
	}

	private IdPropertyInfo getIdPropertyInfo(Class<?> entityType, List<Field> fields) {
		Id idAnnotation = null;
		String idPropertyName = null;
		boolean isIdAutoGenerated = false;
		for (Field field : fields) {
			idAnnotation = AnnotationUtils.findAnnotation(field, Id.class);
			if (idAnnotation != null) {
				idPropertyName = field.getName();
				if (idAnnotation.type() == IdType.AUTO_GENERATED) {
					isIdAutoGenerated = true;
				}
				break;
			}
		}
		if (idAnnotation == null) {
			throw new AnnotationException(
					"@Id annotation not found in class " + entityType.getSimpleName() + " . It is required");
		}
		return new IdPropertyInfo(idPropertyName, isIdAutoGenerated);
	}

	private String getCatalogForTable(Table tableAnnotation) {
		return StringUtils.hasText(tableAnnotation.catalog()) ? tableAnnotation.catalog() : catalogName;
	}

	private String getSchemaForTable(Table tableAnnotation) {
		return StringUtils.hasText(tableAnnotation.schema()) ? tableAnnotation.schema() : schemaName;
	}

}
