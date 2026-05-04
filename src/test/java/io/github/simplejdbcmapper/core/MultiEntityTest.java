package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class MultiEntityTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void MultiEntity_test() {
		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			new MultiEntity().add(null, "t1");
		});
		assertTrue(exception.getMessage().contains("entityType must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			new MultiEntity().add(Order.class, null);
		});
		assertTrue(exception.getMessage().contains("tableAlias has no value"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			new MultiEntity().add(Order.class, "  ");
		});
		assertTrue(exception.getMessage().contains("tableAlias has no value"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			new MultiEntity().add(Order.class, "t.");
		});
		assertTrue(exception.getMessage().contains("tableAlias should be alphanumberic and can include underscore"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			new MultiEntity().add(Order.class, "1x");
		});
		assertTrue(exception.getMessage().contains("tableAlias should start with an alphabet or underscore"));

		// no duplicate table aliases
		exception = Assertions.assertThrows(Exception.class, () -> {
			new MultiEntity().add(Order.class, "t1").add(OrderLine.class, "t1");
		});
		assertTrue(exception.getMessage().contains("duplicate tableAlias"));

		MultiEntity me = new MultiEntity().add(Order.class, "t1");
		exception = Assertions.assertThrows(Exception.class, () -> {
			sjm.getMultiEntitySqlColumns(me);
		});
		assertTrue(exception.getMessage().contains("MultiEntity should have 2 or more entities configured"));

		assertDoesNotThrow(() -> {
			new MultiEntity().add(Order.class, "t1").add(Order.class, "t2");
		});

		assertDoesNotThrow(() -> {
			new MultiEntity().add(Order.class, "t1_").add(Order.class, "_t2");
		});

		assertDoesNotThrow(() -> {
			new MultiEntity().add(Order.class, "t1_2").add(Order.class, "__t2");
		});

	}
}
