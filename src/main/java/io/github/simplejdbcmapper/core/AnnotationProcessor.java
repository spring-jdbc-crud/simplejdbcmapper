package io.github.simplejdbcmapper.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jdbc.core.metadata.TableParameterMetaData;
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

	Table getTableAnnotation(Class<?> clazz) {
		Table tableAnnotation = AnnotationUtils.findAnnotation(clazz, Table.class);
		validateTableAnnotation(tableAnnotation, clazz);
		return tableAnnotation;
	}

	void processColumnAnnotation(Field field, String tableName, Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		Column colAnnotation = AnnotationUtils.findAnnotation(field, Column.class);
		if (colAnnotation != null) {
			String propertyName = field.getName();
			String colName = colAnnotation.name();
			if ("[DEFAULT]".equals(colName)) {
				colName = InternalUtils.toUnderscoreName(propertyName);
			}
			colName = InternalUtils.toLowerCase(colName);
			if (!columnNameToTpmd.containsKey(colName)) {
				throw new AnnotationException(colName + " column not found in table " + tableName + " for property "
						+ field.getDeclaringClass().getSimpleName() + "." + propertyName);
			}
			PropertyMapping propertyMapping = new PropertyMapping(propertyName, field.getType().getName(), colName,
					columnNameToTpmd.get(colName).getSqlType());
			if (colAnnotation.sqlType() != -99999) {
				propertyMapping.setColumnOverriddenSqlType(colAnnotation.sqlType());
			}
			propNameToPropertyMapping.put(propertyName, propertyMapping);
		}
	}

	void processIdAnnotation(Field field, String tableName, Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(Id.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	void processVersionAnnotation(Field field, String tableName, Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(Version.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	void processCreatedOnAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(CreatedOn.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	void processUpdatedOnAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(UpdatedOn.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	void processCreatedByAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(CreatedBy.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	void processUpdatedByAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(UpdatedBy.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	void validateAnnotations(List<PropertyMapping> propertyMappings, Class<?> clazz) {
		annotationDuplicateCheck(propertyMappings, clazz);
		annotationConflictCheck(propertyMappings, clazz);
		annotationVersionTypeCheck(propertyMappings, clazz);
	}

	private <T extends Annotation> void processAnnotation(Class<T> annotationClazz, Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		Annotation annotation = AnnotationUtils.findAnnotation(field, annotationClazz);
		if (annotation != null) {
			String propertyName = field.getName();
			PropertyMapping propMapping = propNameToPropertyMapping.get(propertyName);
			if (propMapping == null) { // it means there is no @Column annotation for the property
				String colName = InternalUtils.toUnderscoreName(propertyName); // the default column name
				if (!columnNameToTpmd.containsKey(colName)) {
					throw new AnnotationException(colName + " column not found in table " + tableName + " for property "
							+ field.getDeclaringClass().getSimpleName() + "." + field.getName());
				}
				propMapping = new PropertyMapping(propertyName, field.getType().getName(), colName,
						columnNameToTpmd.get(colName).getSqlType());
				propNameToPropertyMapping.put(propertyName, propMapping);
			}
			BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(propMapping);
			// set idAnnotation, versionAnnotation, createdOnAnnotation etc on
			// PropertyMapping object
			bw.setPropertyValue(StringUtils.uncapitalize(annotationClazz.getSimpleName()) + "Annotation", true);
		}
	}

	private void validateTableAnnotation(Table tableAnnotation, Class<?> clazz) {
		if (tableAnnotation == null) {
			throw new AnnotationException(
					clazz.getSimpleName() + " does not have the @Table annotation. It is required");
		}
		if (!StringUtils.hasText(tableAnnotation.name())) {
			throw new AnnotationException("For " + clazz.getSimpleName() + " the @Table annotation has a blank name");
		}
	}

	private void annotationDuplicateCheck(List<PropertyMapping> propertyMappings, Class<?> clazz) {
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
			throw new AnnotationException(clazz.getSimpleName() + " has multiple @Id annotations");
		}
		if (versionCnt > 1) {
			throw new AnnotationException(clazz.getSimpleName() + " has multiple @Version annotations");
		}
		if (createdOnCnt > 1) {
			throw new AnnotationException(clazz.getSimpleName() + " has multiple @CreatedOn annotations");
		}
		if (createdByCnt > 1) {
			throw new AnnotationException(clazz.getSimpleName() + " has multiple @CreatedBy annotations");
		}
		if (updatedOnCnt > 1) {
			throw new AnnotationException(clazz.getSimpleName() + " has multiple @UpdatedOn annotations");
		}
		if (updatedByCnt > 1) {
			throw new AnnotationException(clazz.getSimpleName() + " has multiple @UpdatedBy annotations");
		}
	}

	private void annotationConflictCheck(List<PropertyMapping> propertyMappings, Class<?> clazz) {
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
				throw new AnnotationException(clazz.getSimpleName() + "." + propMapping.getPropertyName()
						+ " has multiple annotations that conflict");
			}
		}
	}

	private void annotationVersionTypeCheck(List<PropertyMapping> propertyMappings, Class<?> clazz) {
		for (PropertyMapping propMapping : propertyMappings) {
			if (propMapping.isVersionAnnotation() && !isIntegerClass(propMapping.getPropertyClassName())) {
				throw new AnnotationException("@Version requires the type of property " + clazz.getSimpleName() + "."
						+ propMapping.getPropertyName() + " to be Integer");
			}
		}
	}

	private boolean isIntegerClass(String className) {
		return "java.lang.Integer".equals(className);
	}

}
