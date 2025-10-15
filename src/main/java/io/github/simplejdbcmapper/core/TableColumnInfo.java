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

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.metadata.TableParameterMetaData;

/**
 * Table column info.
 *
 * @author Antony Joseph
 */
class TableColumnInfo {
	private final String tableName;

	private final String schemaName;

	private final String catalogName;

	private List<TableParameterMetaData> tpmdList = new ArrayList<>();

	public TableColumnInfo(String tableName, String schemaName, String catalogName,
			List<TableParameterMetaData> tpmdList) {
		this.tableName = tableName;
		this.schemaName = InternalUtils.isBlank(schemaName) ? null : schemaName;
		this.catalogName = InternalUtils.isBlank(catalogName) ? null : catalogName;
		if (tpmdList != null) {
			this.tpmdList = tpmdList;
		}
	}

	public String getTableName() {
		return tableName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public List<TableParameterMetaData> getTableParameterMetaDataList() {
		return tpmdList;
	}
}
