package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.simplejdbcmapper.model.Product;

@SpringBootTest
class DeleteTest {

  @Autowired private SimpleJdbcMapper sjm;

  @Test
  void deleteByObject_Test() {
    Product product = sjm.findById(Product.class, 4);
    int cnt = sjm.delete(product);
    assertEquals(1, cnt);

    Product product1 = sjm.findById(Product.class, 4);
    assertNull(product1);
  }

  @Test
  void delete_nullObjectFailure_Test() {
    Exception exception =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> {
              sjm.delete(null);
            });
    assertTrue(exception.getMessage().contains("object must not be null"));
  }

  @Test
  void deleteById_Test() {
    int cnt = sjm.deleteById(Product.class, 5);
    assertEquals(1, cnt);

    Product product1 = sjm.findById(Product.class, 5);
    assertNull(product1);
  }

  @Test
  void deleteById_nullId_count_Test() {
    int cnt = sjm.deleteById(Product.class, null);
    assertEquals(0, cnt);
  }
}
