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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * enum for switch statements in the EntityRowMapper so it can be compiled into a 'tableswitch' by
 * java (which it does).
 *
 * @author Antony Joseph
 */
enum ResultSetProcessingType {
  BIGDECIMAL,
  BOOLEAN,
  BYTEARRAY,
  BYTE,
  DATE,
  DOUBLE,
  ENUM,
  FLOAT,
  INTEGER,
  LONG,
  NUMBER,
  SHORT,
  STRING,
  TIMESTAMP,
  TIME,
  UTILDATE,
  CUSTOMCONVERTER,
  UNKNOWN;

  private static final Map<Class<?>, ResultSetProcessingType> map = new HashMap<>();

  static {
    map.put(BigDecimal.class, BIGDECIMAL);
    map.put(Boolean.class, BOOLEAN);
    map.put(byte[].class, BYTEARRAY);
    map.put(Byte.class, BYTE);
    map.put(java.sql.Date.class, DATE);
    map.put(Double.class, DOUBLE);
    map.put(Float.class, FLOAT);
    map.put(Integer.class, INTEGER);
    map.put(Long.class, LONG);
    map.put(Number.class, NUMBER);
    map.put(Short.class, SHORT);
    map.put(String.class, STRING);
    map.put(java.sql.Timestamp.class, TIMESTAMP);
    map.put(java.sql.Time.class, TIME);
    map.put(java.util.Date.class, UTILDATE);
  }

  public static ResultSetProcessingType getResultSetProcessingType(
      Class<?> type, boolean isAttributeWithConverter) {
    if (isAttributeWithConverter) {
      return CUSTOMCONVERTER;
    } else if (type.isEnum()) {
      return ENUM;
    }

    ResultSetProcessingType val = map.get(type);
    return val == null ? UNKNOWN : val;
  }
}
