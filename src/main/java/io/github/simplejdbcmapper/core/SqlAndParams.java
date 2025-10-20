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

import java.util.Set;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * This holds the sql and the sql params needed to issue an update.
 *
 * @author Antony Joseph
 */
class SqlAndParams {
	private final String sql; // the sql string

	private final Set<String> params; // the parameters for the sql

	public SqlAndParams(String sql, Set<String> params) {
		if (!StringUtils.hasText(sql) || ObjectUtils.isEmpty(params)) {
			throw new IllegalArgumentException("sql or params cannot be empty");
		}
		this.sql = sql;
		this.params = params;
	}

	public String getSql() {
		return sql;
	}

	public Set<String> getParams() {
		return params;
	}
}
