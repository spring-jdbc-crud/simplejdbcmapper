package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.OrderLineOrderIdInteger;
import io.github.simplejdbcmapper.model.Product;
import io.github.simplejdbcmapper.relationship.RelationshipMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ToManyTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void toMany_joinOn_validation_test() {
		List<Order> orders = sjm.findAll(Order.class);
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addResult(Order.class, orders, null);
		relMapper.addResult(OrderLine.class, orderLines, null);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Order.class).toMany(OrderLine.class).joinOn(null, "orderId");
		});
		assertTrue(exception.getMessage().contains("mainObjIdProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Order.class).toMany(OrderLine.class).joinOn("orderId", null);
		});
		assertTrue(exception.getMessage().contains("relatedObjFkProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Order.class).toMany(OrderLine.class).joinOn("x", "orderId");
		});
		assertTrue(exception.getMessage().contains("does not exist for"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Order.class).toMany(OrderLine.class).joinOn("orderId", "x");
		});
		assertTrue(exception.getMessage().contains("does not exist for"));

	}

	@Test
	void toMany_joinOn_propertyTypeCheck_test() {
		List<Order> orders = sjm.findAll(Order.class);
		List<OrderLineOrderIdInteger> orderLines = new ArrayList<>();

		orderLines.add(new OrderLineOrderIdInteger());

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addResult(Order.class, orders, null);
		relMapper.addResult(OrderLineOrderIdInteger.class, orderLines, null);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Order.class).toMany(OrderLineOrderIdInteger.class).joinOn("id", "orderId")
					.populate("orderLines");
		});
		assertTrue(exception.getMessage().contains("are not the same"));

	}

	@Test
	void toMany_populate_validation_test() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol");

		String sql = """
					SELECT %s FROM orders o
					LEFT JOIN order_line ol ON o.id =ol.order_id
					WHERE o.id <= 4
					ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId").populate(null);
		});
		assertTrue(exception.getMessage().contains("mainObjPropertyToPopulate must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId").populate("x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

		exception = Assertions.assertThrows(MapperException.class, () -> {
			relMapper.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId").populate("customerId");
		});

		assertTrue(exception.getMessage().contains("argument type mismatch"));

	}

	@Test
	void OrderToManyOrderLines_success() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol");

		String sql = """
				SELECT %s FROM orders o
				LEFT JOIN order_line ol ON o.id = ol.order_id
				WHERE o.id <= 4
				ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = relMapper.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId")
				.populate("orderLines").getList(Order.class);

		assertEquals(4, orders.size());
		assertEquals(2, orders.get(0).getOrderLines().size(), "ord 1 lines count failed");
		assertEquals(0, orders.get(2).getOrderLines().size(), "ord 3 lines count failed");
		assertEquals(1, orders.get(3).getOrderLines().size(), "ord 4 lines count failed");

		assertEquals(5, orders.get(0).getOrderLines().get(1).getNumOfUnits(), "ord 1 OrderLine 2 num_of_units failed");

		assertEquals(1, orders.get(1).getOrderLines().get(0).getNumOfUnits(), "ord 2 OrderLine 3 num_of_units failed");

	}

	@Test
	void toMany_with_MultiEntityPositionSwitched_test_success() {

		// OrderLine first then Order
		MultiEntity multiEntity = new MultiEntity().add(OrderLine.class, "ol").add(Order.class, "o");

		String sql = """
						SELECT %s FROM orders o LEFT JOIN order_line ol ON o.id =
				ol.order_id WHERE o.id <= 4 ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = relMapper.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId")
				.populate("orderLines").getList(Order.class);

		assertEquals(4, orders.size());
		assertEquals(2, orders.get(0).getOrderLines().size(), "ord 1 lines count failed");
		assertEquals(0, orders.get(2).getOrderLines().size(), "ord 3 lines count failed");
		assertEquals(1, orders.get(3).getOrderLines().size(), "ord 4 lines count failed");

		assertEquals(5, orders.get(0).getOrderLines().get(1).getNumOfUnits(), "ord 1 OrderLine 2 num_of_units failed");

		assertEquals(1, orders.get(1).getOrderLines().get(0).getNumOfUnits(), "ord 2 OrderLine 3 num_of_units failed");

	}

	@Test
	void toMany_withNoResultRecords_Test() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol");

		String sql = """

					SELECT %s FROM orders o LEFT JOIN order_line ol ON o.id =
				ol.order_id WHERE o.id <= 0 ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		assertEquals(0, relMapper.getList(Order.class).size());
		assertEquals(0, relMapper.getList(Order.class).size());

		assertDoesNotThrow(() -> {
			relMapper.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId").populate("orderLines");
		});

	}

	@Test
	void ToMany_null_entry_tests() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol");

		String sql = """

						SELECT %s FROM orders o LEFT JOIN order_line ol ON o.id =
				ol.order_id WHERE o.id <= 4 ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = relMapper.getList(Order.class);
		List<OrderLine> orderLines = relMapper.getList(OrderLine.class);

		orders.add(1, null);

		orderLines.add(1, null);

		relMapper.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId").populate("orderLines");

		assertEquals(5, orders.get(0).getOrderLines().get(1).getNumOfUnits());

		assertEquals(1, orders.get(2).getOrderLines().get(0).getNumOfUnits());

	}

	@Test
	void ToMany_NoPropertyValues_entry_tests() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol");

		String sql = """
					 SELECT %s FROM orders o LEFT JOIN order_line ol ON o.id =
				ol.order_id WHERE o.id <= 4 ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = relMapper.getList(Order.class);
		List<OrderLine> orderLines = relMapper.getList(OrderLine.class);

		orders.add(1, new Order());

		orderLines.add(1, new OrderLine());

		relMapper.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId").populate("orderLines");

		assertEquals(5, orders.get(0).getOrderLines().get(1).getNumOfUnits());

		assertEquals(1, orders.get(2).getOrderLines().get(0).getNumOfUnits());

	}

	@Test
	void OrderToManyOrderLinesWhichHasOneProduct_success() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol").add(Product.class,
				"p");

		String sql = """
						SELECT %s
						FROM orders o
						LEFT JOIN order_line ol ON o.id = ol.order_id
						LEFT JOIN product p ON ol.product_id = p.id
						WHERE o.id <= 4 ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		relMapper.type(OrderLine.class).toOne(Product.class).joinOn("productId", "id").populate("product");

		List<Order> orders = relMapper.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId")
				.populate("orderLines").getList(Order.class);

		assertEquals(4, orders.size());
		assertEquals(2, orders.get(0).getOrderLines().size(), "ord 1 lines count failed");
		assertEquals(0, orders.get(2).getOrderLines().size(), "ord 3 lines count failed");
		assertEquals(1, orders.get(3).getOrderLines().size(), "ord 4 lines count failed");

		assertNull(orders.get(3).getOrderLines().get(0).getProduct(),
				"ord 4 first orderline product should be null failed");

		assertEquals("shoes", orders.get(0).getOrderLines().get(0).getProduct().getName(),
				"ord 1 ordline 1 product name failed");
		assertEquals(4.55, orders.get(0).getOrderLines().get(1).getProduct().getCost(),
				"ord 1 ordLine 2 product cost ");

	}

}
