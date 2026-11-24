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

import java.util.Map;
import java.util.StringJoiner;
import org.springframework.util.Assert;

/**
 * gets the sql columns string
 *
 * @author Antony Joseph
 */
class SqlColumnsProvider {
  // Map key - classname
  // value - the column sql string
  private final SimpleCache<Class<?>, String> sqlColumnsCache = new SimpleCache<>();

  // Map key - classname-tableAlias
  // value - the column sql string with columns prefixed with table alias
  private final SimpleCache<String, String> sqlColumnsAliasCache = new SimpleCache<>(5000);

  private final TableMappingProvider tableMappingProvider;

  public SqlColumnsProvider(TableMappingProvider tableMappingProvider) {
    this.tableMappingProvider = tableMappingProvider;
  }

  public String getSqlColumns(Class<?> entityType) {
    Assert.notNull(entityType, "entityType must not be null");
    String columnsSql = sqlColumnsCache.get(entityType);
    if (columnsSql == null) {
      TableMapping tableMapping = tableMappingProvider.getTableMapping(entityType);
      StringJoiner sj = new StringJoiner(", ", " ", " ");
      for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
        sj.add(propMapping.getColumnName());
      }
      columnsSql = sj.toString();
      sqlColumnsCache.put(entityType, columnsSql);
    }
    return columnsSql;
  }

  public String getSqlColumns(Class<?> entityType, String tableAlias) {
    Assert.notNull(entityType, "entityType must not be null");
    InternalUtils.validateTableAlias(tableAlias);
    String cacheKey = entityType.getName() + "-" + tableAlias;
    String columnsSql = sqlColumnsAliasCache.get(cacheKey);
    if (columnsSql == null) {
      String tablePrefix = tableAlias + ".";
      TableMapping tableMapping = tableMappingProvider.getTableMapping(entityType);
      StringJoiner sj = new StringJoiner(", ");
      for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
        sj.add(tablePrefix + propMapping.getColumnName());
      }
      columnsSql = sj.toString();
      sqlColumnsAliasCache.put(cacheKey, columnsSql);
    }
    return columnsSql;
  }

  public String getSqlColumns(MultiEntity multiEntity) {
    Assert.notNull(multiEntity, "multiEntity must not be null");
    StringBuilder sb = new StringBuilder(256);
    int cnt = 0;
    for (Map.Entry<Class<?>, String> entry : multiEntity.getEntries()) {
      if (cnt > 0) {
        sb.append(", ");
      }
      sb.append(getSqlColumns(entry.getKey(), entry.getValue()));
      cnt++;
    }
    return sb.toString();
  }
}
