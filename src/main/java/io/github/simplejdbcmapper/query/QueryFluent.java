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

/**
 * Fluent api to generate query
 *
 * @author Antony Joseph
 */
public interface QueryFluent {
  /** The query interface */
  interface Query {
    /**
     * Returns the sql string
     *
     * <pre>
     * String sql = sjm.select(Product.class).clauses("FROM product WHERE name = ?");
     * </pre>
     *
     * @param clauses The clauses
     * @return String The sql string
     */
    String clauses(String clauses);

    /**
     * Used to add hints immediately after the SELECT statement.
     *
     * <p>To add hints in other locations embed the hints directly in clauses().
     *
     * <pre>
     * String sql = sjm.select(Product.class).hint(&#47;&#42;+ INDEX(product_idx) &#42;&#47;).clauses("FROM product WHERE name = ?");
     *
     * The hint will be added immediately after the SELECT.
     * SELECT &#47;&#42;+ INDEX(product_idx) &#42;&#47; id ...
     * </pre>
     *
     * @param hint The hint
     * @return Clauses the clauses
     */
    Clauses hint(String hint);
  }

  /** The clauses interface */
  interface Clauses {
    /**
     * Returns the sql string
     *
     * <pre>
     * String sql = sjm.select(Product.class).clauses("FROM product WHERE name = ?");
     * </pre>
     *
     * @param clauses The clauses
     * @return String The sql string
     */
    String clauses(String clauses);
  }
}
