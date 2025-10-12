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
 * 
 */
package io.github.simplejdbcmapper.core;

/**
 * A generic table meta data provider.
 */
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.springframework.jdbc.core.metadata.GenericTableMetaDataProvider;

// Spring GenericTableMetaDataProvider constructor is protected so needed to extend
class SjmTableMetaDataProvider extends GenericTableMetaDataProvider {
	public SjmTableMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
	}
}
