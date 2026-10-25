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

import java.lang.reflect.Method;
import java.sql.Types;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * Object property to database column mapping.
 *
 * @author Antony Joseph
 */
class PropertyMapping {

	private String propertyName;

	private Class<?> propertyType;

	private Method writeMethod;

	private Method readMethod;

	private ResultSetProcessingType resultSetProcessingType;

	private String columnName;

	private Integer columnSqlType;

	@SuppressWarnings("rawtypes")
	private AttributeConverter converter;

	private boolean idAnnotation = false;

	private boolean createdOnAnnotation = false;

	private boolean updatedOnAnnotation = false;

	private boolean versionAnnotation = false;

	private boolean createdByAnnotation = false;

	private boolean updatedByAnnotation = false;

	private boolean blobAttribute = false;

	private boolean clobAttribute = false;

	private boolean enumAttribute = false;

	private boolean standardAttribute = false;

	private boolean withConverterAttribute = false;

	@SuppressWarnings("rawtypes")
	public PropertyMapping(String propertyName, Class<?> propertyType, String columnName, Integer columnSqlType,
			Class<?> converterClass) {
		if (propertyName == null || propertyType == null || columnName == null) {
			throw new IllegalArgumentException("propertyName, propertyType, columnName must not be null");
		}
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		// column names stored in lower case always. No plans to support case sensitive
		// table column names or column names with spaces in them
		this.columnName = InternalUtils.toLowerCase(columnName);
		this.columnSqlType = columnSqlType;
		if (converterClass != null) {
			try {
				converter = (AttributeConverter) converterClass.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				throw new MapperException(e);
			}
		}
		determineAttributeProcessingType();
	}

	public int getColumnSqlType() {
		return columnSqlType;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Class<?> getPropertyType() {
		return this.propertyType;
	}

	public String getColumnName() {
		return columnName;
	}

	public boolean isIdAnnotation() {
		return idAnnotation;
	}

	public void setIdAnnotation(boolean idAnnotation) {
		this.idAnnotation = idAnnotation;
	}

	public boolean isCreatedOnAnnotation() {
		return createdOnAnnotation;
	}

	public void setCreatedOnAnnotation(boolean createdOnAnnotation) {
		this.createdOnAnnotation = createdOnAnnotation;
	}

	public boolean isUpdatedOnAnnotation() {
		return updatedOnAnnotation;
	}

	public void setUpdatedOnAnnotation(boolean updatedOnAnnotation) {
		this.updatedOnAnnotation = updatedOnAnnotation;
	}

	public boolean isVersionAnnotation() {
		return versionAnnotation;
	}

	public void setVersionAnnotation(boolean versionAnnotation) {
		this.versionAnnotation = versionAnnotation;
	}

	public boolean isCreatedByAnnotation() {
		return createdByAnnotation;
	}

	public void setCreatedByAnnotation(boolean createdByAnnotation) {
		this.createdByAnnotation = createdByAnnotation;
	}

	public boolean isUpdatedByAnnotation() {
		return updatedByAnnotation;
	}

	public void setUpdatedByAnnotation(boolean updatedByAnnotation) {
		this.updatedByAnnotation = updatedByAnnotation;
	}

	public boolean isBlobAttribute() {
		return blobAttribute;
	}

	public boolean isClobAttribute() {
		return clobAttribute;
	}

	public boolean isEnumAttribute() {
		return enumAttribute;
	}

	public boolean isStandardAttribute() {
		return standardAttribute;
	}

	public boolean isWithConverterAttribute() {
		return withConverterAttribute;
	}

	@SuppressWarnings("rawtypes")
	public AttributeConverter getConverter() {
		return this.converter;
	}

	public Method getReadMethod() {
		return readMethod;
	}

	public void setReadMethod(Method readMethod) {
		this.readMethod = readMethod;
	}

	public Method getWriteMethod() {
		return writeMethod;
	}

	public void setWriteMethod(Method writeMethod) {
		this.writeMethod = writeMethod;
	}

	public ResultSetProcessingType getResultSetProcessingType() {
		return resultSetProcessingType;
	}

	public void setResultSetType(ResultSetProcessingType resultSetProcessingType) {
		this.resultSetProcessingType = resultSetProcessingType;
	}

	private void determineAttributeProcessingType() {
		if (converter != null) {
			withConverterAttribute = true;
		} else if (propertyType.isEnum()) {
			enumAttribute = true;
		} else if (columnSqlType != null && (columnSqlType == Types.BLOB || columnSqlType == Types.ARRAY
				|| columnSqlType == Types.LONGVARBINARY || columnSqlType == Types.VARBINARY)) {
			blobAttribute = true;
		} else if (columnSqlType != null && (columnSqlType == Types.CLOB || columnSqlType == Types.NCLOB
				|| columnSqlType == Types.LONGVARCHAR || columnSqlType == Types.LONGNVARCHAR)) {
			clobAttribute = true;
		} else {
			standardAttribute = true;
		}
	}
}
