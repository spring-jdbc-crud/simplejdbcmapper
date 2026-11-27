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
package io.github.simplejdbcmapper.query;

import org.springframework.util.StringUtils;

/**
 * Creates the SQL query
 *
 * @author Antony Joseph
 */
public class Select implements QueryFluent.Query, QueryFluent.Clauses {
  private final String sqlColumns;
  private String hint = "";

  public Select(String sqlColumns) {
    if (!StringUtils.hasText(sqlColumns)) {
      throw new IllegalArgumentException("sqlColumns should not be empty");
    }
    this.sqlColumns = sqlColumns;
  }

  public QueryFluent.Clauses hint(String hint) {
    if (!StringUtils.hasText(hint)) {
      throw new IllegalArgumentException("hint should not be empty");
    }
    this.hint = hint;
    return this;
  }

  public String clauses(String clauses) {
    if (!StringUtils.hasText(clauses)) {
      throw new IllegalArgumentException("clauses should not be empty");
    }
    return """
    SELECT %s %s
    %s
    """
        .formatted(hint, sqlColumns, clauses);
  }
}
