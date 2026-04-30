package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.Product;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class SimpleJdbcMapperUtilsTest {

	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void populateHasOne_success() {

		assertDoesNotThrow(() -> {
			SimpleJdbcMapperUtils.populateHasOne(new ArrayList<Integer>(), null, "productId", "id", "product");
		});

		assertDoesNotThrow(() -> {
			SimpleJdbcMapperUtils.populateHasOne(null, new ArrayList<Integer>(), "productId", "id", "product");
		});

		List<OrderLine> lines = sjm.findAll(OrderLine.class);
		List<Integer> productIdList = lines.stream().map(OrderLine::getProductId).toList();
		List<Product> products = sjm.findByPropertyValues(Product.class, "id", productIdList);

		// make sure populateHasOne can handle null entries in lists
		lines.add(null);
		products.add(null);
		SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", "id", "product");
		assertNotNull(lines.get(0).getProduct());
		assertEquals(lines.get(0).getProduct().getId(), lines.get(0).getProductId());

	}

	@Test
	void populateHasOne_failure() {
		List<OrderLine> lines = sjm.findAll(OrderLine.class);
		List<Integer> productIdList = lines.stream().map(OrderLine::getProductId).toList();
		String sql = "SELECT " + sjm.getBeanFriendlySqlColumns(Product.class) + " FROM product WHERE id in (:ids)";
		List<Product> products = sjm.getJdbcClient().sql(sql).param("ids", productIdList).query(Product.class).list();

		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, null, "id", "product");
		});
		assertTrue(exception.getMessage().contains("mainObjJoinPropertyNameTheForeignKey must not be null"));

		Exception exception2 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", null, "product");
		});
		assertTrue(exception2.getMessage().contains("relatedObjJoinPropertyNameTheId must not be null"));

		Exception exception3 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", "id", null);
		});
		assertTrue(exception3.getMessage().contains("mainObjHasOnePropertyName must not be null"));

		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "x", "id", "product");
		});

		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", "x", "product");
		});

		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", "id", "x");
		});

		// 'status' type is not Product
		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", "id", "status");
		});

	}

	@Test
	void populateHasMany_success() {

		assertDoesNotThrow(() -> {
			SimpleJdbcMapperUtils.populateHasMany(new ArrayList<Integer>(), null, "orderId", "orderId", "orderLines");
		});

		assertDoesNotThrow(() -> {
			SimpleJdbcMapperUtils.populateHasMany(null, new ArrayList<Integer>(), "orderId", "orderId", "orderLines");
		});

		String sql0 = "SELECT " + sjm.getBeanFriendlySqlColumns(Order.class) + " FROM orders order by id";
		List<Order> orders = sjm.getJdbcClient().sql(sql0).query(Order.class).list();

		List<Long> orderIdList = orders.stream().map(Order::getId).toList();

		List<OrderLine> orderLines = sjm.findByPropertyValues(OrderLine.class, "orderId", orderIdList);

		// make sure populateHasMany() can handle null entries in lists
		orders.add(null);
		orderLines.add(null);
		SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "id", "orderId", "orderLines");

		assertEquals(2, orders.get(0).getOrderLines().size());
		assertEquals(orders.get(0).getId(), orders.get(0).getOrderLines().get(0).getOrderId());
		assertEquals(orders.get(0).getId(), orders.get(0).getOrderLines().get(1).getOrderId());

		assertEquals(1, orders.get(1).getOrderLines().size());
		assertEquals(orders.get(1).getId(), orders.get(1).getOrderLines().get(0).getOrderId());

	}

	@Test
	void populateHasMany_failure() {
		List<Order> orders = sjm.findAll(Order.class);
		List<Long> orderIdList = orders.stream().map(Order::getId).toList();
		String sql = "SELECT " + sjm.getBeanFriendlySqlColumns(OrderLine.class)
				+ " FROM order_line WHERE order_id in (:ids)";
		List<OrderLine> orderLines = sjm.getJdbcClient().sql(sql).param("ids", orderIdList).query(OrderLine.class)
				.list();

		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, null, "orderId", "orderLines");
		});
		assertTrue(exception.getMessage().contains("mainObjJoinPropertyNameTheId must not be null"));

		Exception exception2 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId", null, "orderLines");
		});
		assertTrue(exception2.getMessage().contains("relatedObjJoinPropertyNameTheForeignKey must not be null"));

		Exception exception3 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId", "orderId", null);
		});
		assertTrue(exception3.getMessage().contains("mainObjHasManyPropertyName must not be null"));

		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "x", "orderId", "orderLines");
		});

		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId", "x", "orderLines");
		});

		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId", "orderId", "x");
		});

		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId", "orderId", "status");
		});
	}

	@Test
	void chunkList_test() {
		List<Integer> arr = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		List<List<Integer>> chunkedList = null;
		chunkedList = SimpleJdbcMapperUtils.chunkList(arr, 3);
		assertEquals(4, chunkedList.size());
		assertEquals(1, chunkedList.get(3).size());

		chunkedList = SimpleJdbcMapperUtils.chunkList(arr, 5);
		assertEquals(2, chunkedList.size());
		assertEquals(5, chunkedList.get(1).size());

		chunkedList = SimpleJdbcMapperUtils.chunkList(arr, 10);
		assertEquals(1, chunkedList.size());
		assertEquals(10, chunkedList.get(0).size());

		chunkedList = SimpleJdbcMapperUtils.chunkList(arr, 100);
		assertEquals(1, chunkedList.size());
		assertEquals(10, chunkedList.get(0).size());
	}

}
