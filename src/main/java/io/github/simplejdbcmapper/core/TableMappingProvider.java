/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
import io.github.simplejdbcmapper.exception.MapperException;

/**
 * Provides the table mapping for an object.
 *
 * @author Antony Joseph
 */
class TableMappingProvider {
	private final String schemaName;

	private final String catalogName;

	private final SimpleCache<Class<?>, TableMapping> tableMappingCache = new SimpleCache<>();

	private final AnnotationProcessor ap;

	public TableMappingProvider(String schemaName, String catalogName) {
		this.schemaName = schemaName;
		this.catalogName = catalogName;
		this.ap = new AnnotationProcessor();
	}

	TableMapping getTableMapping(Class<?> entityType) {
		Assert.notNull(entityType, "entityType must not be null");
		TableMapping tableMapping = tableMappingCache.get(entityType);
		if (tableMapping == null) {
			Table tableAnnotation = ap.getTableAnnotation(entityType);
			String tableName = tableAnnotation.name();
			String catalog = getCatalogForTable(tableAnnotation);
			String schema = getSchemaForTable(tableAnnotation);
			List<Field> fields = getAllFields(entityType);
			IdPropertyInfo idPropertyInfo = getIdPropertyInfo(entityType, fields);
			List<PropertyMapping> propertyMappings = getPropertyMappings(entityType, fields);
			tableMapping = new TableMapping(entityType, tableName, schema, catalog, idPropertyInfo, propertyMappings);
			tableMappingCache.put(entityType, tableMapping);
		}
		return tableMapping;
	}

	SimpleCache<Class<?>, TableMapping> getTableMappingCache() {
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
		assignResultSetTypes(propertyMappings);
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
		try {
			BeanWrapperImpl bw = new BeanWrapperImpl(entityType);
			for (PropertyMapping propMapping : propertyMappings) {
				PropertyDescriptor pd = bw.getPropertyDescriptor(propMapping.getPropertyName());
				Method writeMethod = pd.getWriteMethod();
				if (writeMethod == null) {
					throw new MapperException("setter method was not accessible for property " + entityType.getName()
							+ "." + propMapping.getPropertyName() + " Check the method's visibility.");
				}
				propMapping.setWriteMethod(writeMethod);

				Method readMethod = pd.getReadMethod();
				if (readMethod == null) {
					throw new MapperException("getter method was not accessible for property " + entityType.getName()
							+ "." + propMapping.getPropertyName() + " Check the method's visibility.");
				}
				propMapping.setReadMethod(readMethod);

			}
		} catch (Exception e) {
			throw new MapperException(e.getMessage(), e);
		}
	}

	private void assignResultSetTypes(List<PropertyMapping> propertyMappings) {
		for (PropertyMapping propMapping : propertyMappings) {
			ResultSetType rsType = ResultSetType.getResultSetType(propMapping.getPropertyType());
			propMapping.setResultSetType(rsType);
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
