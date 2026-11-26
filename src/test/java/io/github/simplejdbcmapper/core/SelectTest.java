package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.Product;
import io.github.simplejdbcmapper.relationship.Relationship;
import io.github.simplejdbcmapper.relationship.RelationshipMapper;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SelectTest {
  @Autowired private SimpleJdbcMapper sjm;

  @Test
  void select_clause_failure() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          sjm.select(Product.class).clauses(null);
        });

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          sjm.select(Product.class).clauses("  ");
        });
  }

  @Test
  void select_hint_failure() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          sjm.select(Product.class).hint(null);
        });

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          sjm.select(Product.class).hint("  ");
        });
  }

  @Test
  void select_hint_success() {
    String sql =
        sjm.select(Product.class)
            .hint("/*+ INDEX(product_idx) */")
            .clauses("FROM product WHERE id = ?");

    assertTrue(sql.contains("SELECT /*+ INDEX(product_idx) */"));
    assertTrue(sql.contains("FROM product WHERE id = ?"));
  }

  @Test
  void select_mutliEntity_success() {
    MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol");

    String sql =
        sjm.select(multiEntity)
            .clauses(
                """
                  FROM orders o
                  LEFT JOIN order_line ol ON o.id = ol.order_id
                  WHERE o.id <= 4
                  ORDER BY o.id, ol.order_line_id
                """);

    RelationshipMapper relMapper =
        sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

    Relationship orderToManyOrderLines =
        Relationship.type(Order.class)
            .toMany(OrderLine.class)
            .joinOn("id", "orderId")
            .populate("orderLines");

    List<Order> orders = relMapper.assemble(orderToManyOrderLines).getList(Order.class);

    assertEquals(4, orders.size());
  }

  @Test
  void select_singleEntity_success() {
    String sql = sjm.select(Product.class).clauses("FROM product WHERE id = ?");

    List<Product> products =
        sjm.getJdbcTemplate().query(sql, sjm.entityRowMapper(Product.class), 1);

    assertEquals(1, products.get(0).getId());
  }

  @Test
  void select_singleEntityAlias_success() {
    String sql = sjm.select(Product.class, "p").clauses("FROM product p WHERE id = ?");

    Assertions.assertDoesNotThrow(
        () -> sjm.getJdbcTemplate().query(sql, sjm.entityRowMapper(Product.class), 1));
  }
}
