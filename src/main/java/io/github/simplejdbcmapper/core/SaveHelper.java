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

import io.github.simplejdbcmapper.exception.MapperException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.support.SqlBinaryValue;
import org.springframework.jdbc.core.support.SqlCharacterValue;

/**
 * Helper for insert/update
 *
 * @author Antony Joseph
 */
class SaveHelper {
  static void addMapSqlParameterSource(
      MapSqlParameterSource mapSqlParameterSource,
      Object val,
      String paramName,
      PropertyMapping propMapping) {
    Integer columnSqlType = propMapping.getColumnSqlType();
    if (propMapping.isStandardAttribute()) {
      mapSqlParameterSource.addValue(paramName, val, columnSqlType);
    } else if (propMapping.isEnumAttribute()) {
      assignEnumMapSqlParameterSource(mapSqlParameterSource, val, paramName, columnSqlType);
    } else if (propMapping.isBlobAttribute()) {
      assignBlobMapSqlParameterSource(mapSqlParameterSource, val, paramName, columnSqlType);
    } else if (propMapping.isClobAttribute()) {
      assignClobMapSqlParameterSource(mapSqlParameterSource, val, paramName, columnSqlType);
    } else if (propMapping.isWithConverterAttribute()) {
      assignMapSqlParameterSourceWithConverter(
          mapSqlParameterSource, val, paramName, columnSqlType, propMapping.getConverter());
    }
  }

  static void assignBlobMapSqlParameterSource(
      MapSqlParameterSource mapSqlParameterSource,
      Object val,
      String paramName,
      Integer columnSqlType) {
    if (val == null) {
      mapSqlParameterSource.addValue(paramName, null, columnSqlType);
    } else if (val instanceof byte[] byteArray) {
      mapSqlParameterSource.addValue(paramName, new SqlBinaryValue(byteArray), columnSqlType);
    } else {
      throw new MapperException(
          val.getClass().getSimpleName()
              + " "
              + paramName
              + " : java type has to be byte[] for a BLOB mapping. No other type is supported.");
    }
  }

  static void assignClobMapSqlParameterSource(
      MapSqlParameterSource mapSqlParameterSource,
      Object val,
      String paramName,
      Integer columnSqlType) {
    if (val == null) {
      mapSqlParameterSource.addValue(paramName, null, columnSqlType);
    } else if (val instanceof String str) {
      mapSqlParameterSource.addValue(paramName, new SqlCharacterValue(str), columnSqlType);
    } else {
      throw new MapperException(
          val.getClass().getSimpleName()
              + "."
              + paramName
              + " : java type has to be String for a CLOB mapping. No other type is supported");
    }
  }

  static void assignEnumMapSqlParameterSource(
      MapSqlParameterSource mapSqlParameterSource,
      Object val,
      String paramName,
      Integer columnSqlType) {
    if (val == null) {
      mapSqlParameterSource.addValue(paramName, null, columnSqlType);
    } else {
      mapSqlParameterSource.addValue(paramName, ((Enum<?>) val).name(), columnSqlType);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static void assignMapSqlParameterSourceWithConverter(
      MapSqlParameterSource mapSqlParameterSource,
      Object val,
      String paramName,
      Integer columnSqlType,
      AttributeConverter converter) {
    if (val == null) {
      mapSqlParameterSource.addValue(paramName, null, columnSqlType);
    } else {
      mapSqlParameterSource.addValue(
          paramName, converter.convertToDatabaseColumn(val), columnSqlType);
    }
  }

  private SaveHelper() {}
}
