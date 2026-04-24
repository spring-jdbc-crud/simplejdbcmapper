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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.Clob;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.CreatedBy;
import io.github.simplejdbcmapper.annotation.CreatedOn;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.annotation.UpdatedBy;
import io.github.simplejdbcmapper.annotation.UpdatedOn;
import io.github.simplejdbcmapper.annotation.Version;
import io.github.simplejdbcmapper.exception.AnnotationException;

class AnnotationProcessor {

	Table getTableAnnotation(Class<?> entityType) {
		Table tableAnnotation = AnnotationUtils.findAnnotation(entityType, Table.class);
		validateTableAnnotation(tableAnnotation, entityType);
		return tableAnnotation;
	}

	void processColumnAnnotation(Field field, Map<String, PropertyMapping> propNameToPropertyMapping) {
		Column colAnnotation = AnnotationUtils.findAnnotation(field, Column.class);
		if (colAnnotation != null) {
			String propertyName = field.getName();
			String colName = colAnnotation.name();
			if ("[DEFAULT]".equals(colName)) {
				colName = InternalUtils.toUnderscoreName(propertyName);
			}
			colName = InternalUtils.toLowerCase(colName);
			Integer sqlType = InternalUtils.javaTypeToSqlParameterType(field.getType());
			PropertyMapping propertyMapping = null;
			if (colAnnotation.sqlType() != Integer.MIN_VALUE) {
				// sqlType has been configured in @Column
				propertyMapping = new PropertyMapping(propertyName, field.getType(), colName, sqlType,
						colAnnotation.sqlType());
			} else {
				propertyMapping = new PropertyMapping(propertyName, field.getType(), colName, sqlType);
			}
			propNameToPropertyMapping.put(propertyName, propertyMapping);
		}
	}

	void processIdAnnotation(Field field, Map<String, PropertyMapping> propNameToPropertyMapping) {
		processAnnotation(Id.class, field, propNameToPropertyMapping);
	}

	void processVersionAnnotation(Field field, Map<String, PropertyMapping> propNameToPropertyMapping) {
		processAnnotation(Version.class, field, propNameToPropertyMapping);
	}

	void processCreatedOnAnnotation(Field field, Map<String, PropertyMapping> propNameToPropertyMapping) {
		processAnnotation(CreatedOn.class, field, propNameToPropertyMapping);
	}

	void processUpdatedOnAnnotation(Field field, Map<String, PropertyMapping> propNameToPropertyMapping) {
		processAnnotation(UpdatedOn.class, field, propNameToPropertyMapping);
	}

	void processCreatedByAnnotation(Field field, Map<String, PropertyMapping> propNameToPropertyMapping) {
		processAnnotation(CreatedBy.class, field, propNameToPropertyMapping);
	}

	void processUpdatedByAnnotation(Field field, Map<String, PropertyMapping> propNameToPropertyMapping) {
		processAnnotation(UpdatedBy.class, field, propNameToPropertyMapping);
	}

	void validateAnnotations(List<PropertyMapping> propertyMappings, Class<?> type) {
		annotationTypeCheck(propertyMappings, type);
		annotationDuplicateCheck(propertyMappings, type);
		annotationConflictCheck(propertyMappings, type);
	}

	private <T extends Annotation> void processAnnotation(Class<T> annotationType, Field field,
			Map<String, PropertyMapping> propNameToPropertyMapping) {
		Annotation annotation = AnnotationUtils.findAnnotation(field, annotationType);
		if (annotation != null) {
			String propertyName = field.getName();
			PropertyMapping propMapping = propNameToPropertyMapping.get(propertyName);
			if (propMapping == null) { // it means there is no @Column annotation for the property
				String colName = InternalUtils.toUnderscoreName(propertyName); // the default column name
				Integer sqlType = InternalUtils.javaTypeToSqlParameterType(field.getType());
				propMapping = new PropertyMapping(propertyName, field.getType(), colName, sqlType);
				propNameToPropertyMapping.put(propertyName, propMapping);
			}
			BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(propMapping);
			// set idAnnotation, versionAnnotation, createdOnAnnotation etc on
			// PropertyMapping object
			bw.setPropertyValue(StringUtils.uncapitalize(annotationType.getSimpleName()) + "Annotation", true);
		}
	}

	private void validateTableAnnotation(Table tableAnnotation, Class<?> entityType) {
		if (tableAnnotation == null) {
			throw new AnnotationException(
					entityType.getSimpleName() + " does not have the @Table annotation. It is required");
		}
		if (!StringUtils.hasText(tableAnnotation.name())) {
			throw new AnnotationException(
					"For " + entityType.getSimpleName() + " the @Table annotation has a blank name");
		}
	}

	private void annotationTypeCheck(List<PropertyMapping> propertyMappings, Class<?> entityType) {
		for (PropertyMapping propMapping : propertyMappings) {
			if (propMapping.getPropertyType().isPrimitive()) {
				throw new AnnotationException(entityType.getSimpleName() + "." + propMapping.getPropertyName()
						+ " is a primitive. Mapper does not support primitive types. Use the corresponding java wrapper type.");
			} else if (propMapping.getPropertyType() == Blob.class) {
				throw new AnnotationException(entityType.getSimpleName() + "." + propMapping.getPropertyName()
						+ " is of type java.sql.Blob and is not supported.");
			} else if (propMapping.getPropertyType() == Clob.class) {
				throw new AnnotationException(entityType.getSimpleName() + "." + propMapping.getPropertyName()
						+ " is of type java.sql.Clob and is not supported.");
			} else if (propMapping.isVersionAnnotation() && propMapping.getPropertyType() != Integer.class) {
				throw new AnnotationException("@Version requires the type of property " + entityType.getSimpleName()
						+ "." + propMapping.getPropertyName() + " to be Integer");
			}
		}
	}

	private void annotationDuplicateCheck(List<PropertyMapping> propertyMappings, Class<?> entityType) {
		int idCnt = 0;
		int versionCnt = 0;
		int createdByCnt = 0;
		int createdOnCnt = 0;
		int updatedOnCnt = 0;
		int updatedByCnt = 0;
		for (PropertyMapping propMapping : propertyMappings) {
			if (propMapping.isIdAnnotation()) {
				idCnt++;
			}
			if (propMapping.isVersionAnnotation()) {
				versionCnt++;
			}
			if (propMapping.isCreatedOnAnnotation()) {
				createdOnCnt++;
			}
			if (propMapping.isCreatedByAnnotation()) {
				createdByCnt++;
			}
			if (propMapping.isUpdatedOnAnnotation()) {
				updatedOnCnt++;
			}
			if (propMapping.isUpdatedByAnnotation()) {
				updatedByCnt++;
			}
		}
		if (idCnt > 1) {
			throw new AnnotationException(entityType.getSimpleName() + " has multiple @Id annotations");
		}
		if (versionCnt > 1) {
			throw new AnnotationException(entityType.getSimpleName() + " has multiple @Version annotations");
		}
		if (createdOnCnt > 1) {
			throw new AnnotationException(entityType.getSimpleName() + " has multiple @CreatedOn annotations");
		}
		if (createdByCnt > 1) {
			throw new AnnotationException(entityType.getSimpleName() + " has multiple @CreatedBy annotations");
		}
		if (updatedOnCnt > 1) {
			throw new AnnotationException(entityType.getSimpleName() + " has multiple @UpdatedOn annotations");
		}
		if (updatedByCnt > 1) {
			throw new AnnotationException(entityType.getSimpleName() + " has multiple @UpdatedBy annotations");
		}
	}

	private void annotationConflictCheck(List<PropertyMapping> propertyMappings, Class<?> entityType) {
		for (PropertyMapping propMapping : propertyMappings) {
			int conflictCnt = 0;
			if (propMapping.isIdAnnotation()) {
				conflictCnt++;
			}
			if (propMapping.isVersionAnnotation()) {
				conflictCnt++;
			}
			if (propMapping.isCreatedOnAnnotation()) {
				conflictCnt++;
			}
			if (propMapping.isCreatedByAnnotation()) {
				conflictCnt++;
			}
			if (propMapping.isUpdatedOnAnnotation()) {
				conflictCnt++;
			}
			if (propMapping.isUpdatedByAnnotation()) {
				conflictCnt++;
			}
			if (conflictCnt > 1) {
				throw new AnnotationException(entityType.getSimpleName() + "." + propMapping.getPropertyName()
						+ " has multiple annotations that conflict");
			}
		}
	}

}
