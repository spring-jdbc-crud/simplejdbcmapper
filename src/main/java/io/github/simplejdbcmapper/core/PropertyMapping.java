/*
 * Copyright 2025 the original author or authors.
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

import io.github.simplejdbcmapper.type.TypeHandler;

/**
 * Object property to database column mapping.
 *
 * @author Antony Joseph
 */
class PropertyMapping {
	private String propertyName;

	private Class<?> propertyType;

	private Method writeMethod; // writeMethod for property to be set by reflection

	private TypeHandler typeHandler;

	private int resultSetType;

	private String columnName;

	private Integer columnSqlType; // java.sql.Types from database meta data.

	private Integer columnOverriddenSqlType;

	private boolean isEnum = false;

	private boolean idAnnotation = false;

	private boolean createdOnAnnotation = false;

	private boolean updatedOnAnnotation = false;

	private boolean versionAnnotation = false;

	private boolean createdByAnnotation = false;

	private boolean updatedByAnnotation = false;

	private boolean binaryLargeObject = false;

	private boolean characterLargeObject = false;

	public PropertyMapping(String propertyName, Class<?> propertyType, String columnName, Integer columnSqlType) {
		this(propertyName, propertyType, columnName, columnSqlType, null);
	}

	public PropertyMapping(String propertyName, Class<?> propertyType, String columnName, Integer columnSqlType,
			Integer columnOverriddenSqlType) {
		if (propertyName == null || propertyType == null || columnName == null) {
			throw new IllegalArgumentException("propertyName, propertyType, columnName must not be null");
		}
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		// column names stored in lower case always. No plans to support case sensitive
		// table column names or column names with spaces in them
		this.columnName = InternalUtils.toLowerCase(columnName);
		this.columnSqlType = columnSqlType;
		this.columnOverriddenSqlType = columnOverriddenSqlType;
		isEnum = propertyType.isEnum();
		determineBlobClob();
	}

	public int getColumnSqlType() {
		return columnSqlType;
	}

	public Integer getColumnOverriddenSqlType() {
		return columnOverriddenSqlType;
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

	public Integer getEffectiveSqlType() {
		return columnOverriddenSqlType == null ? columnSqlType : columnOverriddenSqlType;
	}

	public boolean isBinaryLargeObject() {
		return binaryLargeObject;
	}

	public boolean isCharacterLargeObject() {
		return characterLargeObject;
	}

	public boolean isEnum() {
		return isEnum;
	}

	public Method getWriteMethod() {
		return writeMethod;
	}

	public void setWriteMethod(Method writeMethod) {
		this.writeMethod = writeMethod;
	}

	public TypeHandler getTypeHandler() {
		return typeHandler;
	}

	public void setTypeHandler(TypeHandler typeHandler) {
		this.typeHandler = typeHandler;
	}

	public int getResultSetType() {
		return resultSetType;
	}

	public void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}

	private void determineBlobClob() {
		Integer effectiveSqlType = getEffectiveSqlType();
		if (effectiveSqlType != null && (effectiveSqlType == Types.BLOB || effectiveSqlType == Types.ARRAY
				|| effectiveSqlType == Types.LONGVARBINARY || effectiveSqlType == Types.VARBINARY)) {
			binaryLargeObject = true;
		} else if (effectiveSqlType != null && (effectiveSqlType == Types.CLOB || effectiveSqlType == Types.NCLOB
				|| effectiveSqlType == Types.LONGVARCHAR || effectiveSqlType == Types.LONGNVARCHAR)) {
			characterLargeObject = true;
		}
	}
}
