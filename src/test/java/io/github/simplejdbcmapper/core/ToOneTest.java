package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.Product;
import io.github.simplejdbcmapper.relationship.Relationship;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ToOneTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void toOne_joinOn_validation_test() {
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);
		List<Product> products = sjm.findAll(Product.class);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(orderLines).toOneList(products).joinOn(null, "productId");
		});
		assertTrue(exception.getMessage().contains("mainObjJoinProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(orderLines).toOneList(products).joinOn("orderLineId", null);
		});
		assertTrue(exception.getMessage().contains("relatedObjJoinProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(orderLines).toOneList(products).joinOn("x", "productId");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name "));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(orderLines).toOneList(products).joinOn("orderLineId", "x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name "));

		assertDoesNotThrow(() -> {
			Relationship.mainList(null).toOneList(products).joinOn("x", "productId");
		});

		assertDoesNotThrow(() -> {
			Relationship.mainList(orderLines).toOneList(null).joinOn("orderLineId", "x");
		});

	}

	@Test
	void toOne_populate_validation_test() {
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);
		List<Product> products = sjm.findAll(Product.class);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(orderLines).toOneList(products).joinOn("orderLineId", "productId").populate(null);
		});
		assertTrue(exception.getMessage().contains("mainObjPropertyToPopulate must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(orderLines).toOneList(products).joinOn("orderLineId", "productId").populate("x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

		assertDoesNotThrow(() -> {
			Relationship.mainList(null).toOneList(products).joinOn("orderLineId", "productId").populate("x");
		});

	}
}
