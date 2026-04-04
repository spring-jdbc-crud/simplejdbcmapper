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

import java.sql.Types;

/**
 * Object property to database column mapping.
 *
 * @author Antony Joseph
 */
class PropertyMapping {
	private String propertyName;

	private String propertyClassName;

	private String columnName;

	private int columnSqlType; // java.sql.Types from database meta data.

	private Integer columnOverriddenSqlType;

	private boolean idAnnotation = false;

	private boolean createdOnAnnotation = false;

	private boolean updatedOnAnnotation = false;

	private boolean versionAnnotation = false;

	private boolean createdByAnnotation = false;

	private boolean updatedByAnnotation = false;

	private boolean binaryLargeObject = false;

	private boolean characterLargeObject = false;

	public PropertyMapping(String propertyName, Class<?> propertyType, String columnName, int columnSqlType) {
		this(propertyName, propertyType, columnName, columnSqlType, null);
	}

	public PropertyMapping(String propertyName, Class<?> propertyType, String columnName, int columnSqlType,
			Integer columnOverriddenSqlType) {
		if (propertyName == null || propertyType == null || columnName == null) {
			throw new IllegalArgumentException("propertyName, propertyType, columnName must not be null");
		}
		this.propertyName = propertyName;
		this.propertyClassName = propertyType.getName();
		// column names stored in lower case always. No plans to support case sensitive
		// table column names or column names with spaces in them
		this.columnName = InternalUtils.toLowerCase(columnName);
		this.columnSqlType = columnSqlType;
		this.columnOverriddenSqlType = columnOverriddenSqlType;
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

	public String getPropertyClassName() {
		return propertyClassName;
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

	public int getEffectiveSqlType() {
		return columnOverriddenSqlType == null ? columnSqlType : columnOverriddenSqlType;
	}

	public boolean isBinaryLargeObject() {
		return binaryLargeObject;
	}

	public boolean isCharacterLargeObject() {
		return characterLargeObject;
	}

	private void determineBlobClob() {
		if (byte[].class.getName().equals(propertyClassName)) {
			binaryLargeObject = true;
		} else if (char[].class.getName().equals(propertyClassName)) {
			characterLargeObject = true;
		} else {
			int effectiveSqlType = getEffectiveSqlType();
			if (effectiveSqlType == Types.CLOB || effectiveSqlType == Types.NCLOB
					|| effectiveSqlType == Types.LONGVARCHAR || effectiveSqlType == Types.LONGNVARCHAR) {
				characterLargeObject = true;
			}
		}
	}

}
