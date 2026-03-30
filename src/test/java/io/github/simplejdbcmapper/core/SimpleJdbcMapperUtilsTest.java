package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.OrderWithRawCollection;
import io.github.simplejdbcmapper.model.Product;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class SimpleJdbcMapperUtilsTest {

	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void populateHasOne_success() {

		assertDoesNotThrow(() -> {
			SimpleJdbcMapperUtils.populateHasOne(new ArrayList<Integer>(), null, "productId", "productId", "product");
		});

		assertDoesNotThrow(() -> {
			SimpleJdbcMapperUtils.populateHasOne(null, new ArrayList<Integer>(), "productId", "productId", "product");
		});

		List<OrderLine> lines = sjm.findAll(OrderLine.class);
		List<Integer> productIdList = lines.stream().map(OrderLine::getProductId).toList();
		List<Product> products = sjm.findByPropertyValues(Product.class, "productId", productIdList);

		// make sure populateHasOne can handle null entries in lists
		lines.add(null);
		products.add(null);
		SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", "productId", "product");
		assertNotNull(lines.get(0).getProduct());
		assertEquals(lines.get(0).getProduct().getProductId(), lines.get(0).getProductId());

	}

	@Test
	void populateHasOne_failure() {
		List<OrderLine> lines = sjm.findAll(OrderLine.class);
		List<Integer> productIdList = lines.stream().map(OrderLine::getProductId).toList();
		String sql = "SELECT " + sjm.getBeanFriendlySqlColumns(Product.class)
				+ " FROM product WHERE product_id in (:ids)";
		List<Product> products = sjm.getJdbcClient().sql(sql).param("ids", productIdList).query(Product.class).list();

		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, null, "productId", "product");
		});
		assertTrue(exception.getMessage().contains("parentJoinPropertyName must not be null"));

		Exception exception2 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", null, "product");
		});
		assertTrue(exception2.getMessage().contains("childJoinPropertyName must not be null"));

		Exception exception3 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", "productId", null);
		});
		assertTrue(exception3.getMessage().contains("parentHasOnePropertyName must not be null"));

		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "x", "productId", "product");
		});

		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", "x", "product");
		});

		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", "productId", "x");
		});

		// 'status' type is not Product
		Assertions.assertThrows(Exception.class, () -> {
			SimpleJdbcMapperUtils.populateHasOne(lines, products, "productId", "productId", "status");
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

		String sql0 = "SELECT " + sjm.getBeanFriendlySqlColumns(Order.class) + " FROM orders order by order_id";
		List<Order> orders = sjm.getJdbcClient().sql(sql0).query(Order.class).list();

		List<Long> orderIdList = orders.stream().map(Order::getOrderId).toList();

		List<OrderLine> orderLines = sjm.findByPropertyValues(OrderLine.class, "orderId", orderIdList);

		// make sure populateHasMany() can handle null entries in lists
		orders.add(null);
		orderLines.add(null);
		SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId", "orderId", "orderLines");

		assertEquals(2, orders.get(0).getOrderLines().size());
		assertEquals(orders.get(0).getOrderId(), orders.get(0).getOrderLines().get(0).getOrderId());
		assertEquals(orders.get(0).getOrderId(), orders.get(0).getOrderLines().get(1).getOrderId());

		assertEquals(1, orders.get(1).getOrderLines().size());
		assertEquals(orders.get(1).getOrderId(), orders.get(1).getOrderLines().get(0).getOrderId());

	}

	@Test
	void populateHasMany_failure() {
		List<Order> orders = sjm.findAll(Order.class);
		List<Long> orderIdList = orders.stream().map(Order::getOrderId).toList();
		String sql = "SELECT " + sjm.getBeanFriendlySqlColumns(OrderLine.class)
				+ " FROM order_line WHERE order_id in (:ids)";
		List<OrderLine> orderLines = sjm.getJdbcClient().sql(sql).param("ids", orderIdList).query(OrderLine.class)
				.list();

		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, null, "orderId", "orderLines");
		});
		assertTrue(exception.getMessage().contains("parentJoinPropertyName must not be null"));

		Exception exception2 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId", null, "orderLines");
		});
		assertTrue(exception2.getMessage().contains("childJoinPropertyName must not be null"));

		Exception exception3 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId", "orderId", null);
		});
		assertTrue(exception3.getMessage().contains("parentHasManyPropertyName must not be null"));

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

	@Deprecated
	@Test
	void mergeResultsToPopulateHasOne_success() {
		List<OrderLine> lines = sjm.findAll(OrderLine.class);
		List<Integer> productIdList = lines.stream().map(OrderLine::getProductId).toList();

		List<Product> products = sjm.findByPropertyValues(Product.class, "productId", productIdList);

		SimpleJdbcMapperUtils.mergeResultsToPopulateHasOne(lines, products, "productId", "productId", "product");
		assertNotNull(lines.get(0).getProduct());
		assertEquals(lines.get(0).getProduct().getProductId(), lines.get(0).getProductId());

	}

	@Deprecated
	@Test
	void mergeResultsToPopulateHasOne_failure() {
		List<OrderLine> lines = sjm.findAll(OrderLine.class);
		List<Integer> productIdList = lines.stream().map(OrderLine::getProductId).toList();
		String sql = "SELECT " + sjm.getBeanFriendlySqlColumns(Product.class)
				+ " FROM product WHERE product_id in (:ids)";
		List<Product> products = sjm.getJdbcClient().sql(sql).param("ids", productIdList).query(Product.class).list();

		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasOne(lines, products, null, "productId", "product");
		});
		assertTrue(exception.getMessage().contains("parentJoinPropertyName must not be null"));

		Exception exception2 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasOne(lines, products, "productId", null, "product");
		});
		assertTrue(exception2.getMessage().contains("childJoinPropertyName must not be null"));

		Exception exception3 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasOne(lines, products, "productId", "productId", null);
		});
		assertTrue(exception3.getMessage().contains("parentHasOnePropertyName must not be null"));

		Exception exception4 = Assertions.assertThrows(MapperException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasOne(lines, products, "x", "productId", "product");
		});
		assertTrue(exception4.getMessage().contains("not found in"));

		Exception exception5 = Assertions.assertThrows(MapperException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasOne(lines, products, "productId", "x", "product");
		});
		assertTrue(exception5.getMessage().contains("not found in"));

		Exception exception6 = Assertions.assertThrows(MapperException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasOne(lines, products, "productId", "productId", "x");
		});
		assertTrue(exception6.getMessage().contains("not found in"));

		Exception exception7 = Assertions.assertThrows(MapperException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasOne(lines, products, "productId", "productId", "status");
		});
		assertTrue(exception7.getMessage().contains("property type conflict"));

	}

	@Deprecated
	@Test
	void mergeResultsToPopulateHasMany_success() {
		String sql0 = "SELECT " + sjm.getBeanFriendlySqlColumns(Order.class) + " FROM orders order by order_id";
		List<Order> orders = sjm.getJdbcClient().sql(sql0).query(Order.class).list();

		List<Long> orderIdList = orders.stream().map(Order::getOrderId).toList();

		List<OrderLine> orderLines = sjm.findByPropertyValues(OrderLine.class, "orderId", orderIdList);

		SimpleJdbcMapperUtils.mergeResultsToPopulateHasMany(orders, orderLines, "orderId", "orderId", "orderLines");

		assertEquals(2, orders.get(0).getOrderLines().size());
		assertEquals(orders.get(0).getOrderId(), orders.get(0).getOrderLines().get(0).getOrderId());
		assertEquals(orders.get(0).getOrderId(), orders.get(0).getOrderLines().get(1).getOrderId());

		assertEquals(1, orders.get(1).getOrderLines().size());
		assertEquals(orders.get(1).getOrderId(), orders.get(1).getOrderLines().get(0).getOrderId());

	}

	@Deprecated
	@Test
	void mergeResultsToPopulateHasMany_failure() {
		List<Order> orders = sjm.findAll(Order.class);
		List<Long> orderIdList = orders.stream().map(Order::getOrderId).toList();
		String sql = "SELECT " + sjm.getBeanFriendlySqlColumns(OrderLine.class)
				+ " FROM order_line WHERE order_id in (:ids)";
		List<OrderLine> orderLines = sjm.getJdbcClient().sql(sql).param("ids", orderIdList).query(OrderLine.class)
				.list();

		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasMany(orders, orderLines, null, "orderId", "orderLines");
		});
		assertTrue(exception.getMessage().contains("parentJoinPropertyName must not be null"));

		Exception exception2 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasMany(orders, orderLines, "orderId", null, "orderLines");
		});
		assertTrue(exception2.getMessage().contains("childJoinPropertyName must not be null"));

		Exception exception3 = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasMany(orders, orderLines, "orderId", "orderId", null);
		});
		assertTrue(exception3.getMessage().contains("parentHasManyPropertyName must not be null"));

		Exception exception4 = Assertions.assertThrows(MapperException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasMany(orders, orderLines, "x", "orderId", "orderLines");
		});
		assertTrue(exception4.getMessage().contains("not found in"));

		Exception exception5 = Assertions.assertThrows(MapperException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasMany(orders, orderLines, "orderId", "x", "orderLines");
		});
		assertTrue(exception5.getMessage().contains("not found in"));

		Exception exception6 = Assertions.assertThrows(MapperException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasMany(orders, orderLines, "orderId", "orderId", "x");
		});
		assertTrue(exception6.getMessage().contains("not found in"));

		Exception exception7 = Assertions.assertThrows(MapperException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasMany(orders, orderLines, "orderId", "orderId", "status");
		});
		assertTrue(exception7.getMessage().contains("is not a collection"));

		// OrderWithRawCollection has a raw list
		List<OrderWithRawCollection> orderWithRawCollections = sjm.findAll(OrderWithRawCollection.class);
		Exception exception8 = Assertions.assertThrows(MapperException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasMany(orderWithRawCollections, orderLines, "orderId",
					"orderId", "orderLines");
		});
		assertTrue(exception8.getMessage().contains("Collections without generic types are not supported"));

		List<Product> products = List.of(new Product());
		Exception exception9 = Assertions.assertThrows(MapperException.class, () -> {
			SimpleJdbcMapperUtils.mergeResultsToPopulateHasMany(orders, products, "orderId", "productId", "orderLines");
		});
		assertTrue(exception9.getMessage().contains("Collection generic type and child class type mismatch"));
	}

	@Deprecated
	@Test
	void chunkTheList_test() {
		Integer[] arr = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		@SuppressWarnings("rawtypes")
		List<List> chunkedList = null;
		chunkedList = SimpleJdbcMapperUtils.chunkTheList(Arrays.asList(arr), 3);
		assertEquals(4, chunkedList.size());
		assertEquals(1, chunkedList.get(3).size());

		chunkedList = SimpleJdbcMapperUtils.chunkTheList(Arrays.asList(arr), 5);
		assertEquals(2, chunkedList.size());
		assertEquals(5, chunkedList.get(1).size());

		chunkedList = SimpleJdbcMapperUtils.chunkTheList(Arrays.asList(arr), 10);
		assertEquals(1, chunkedList.size());
		assertEquals(10, chunkedList.get(0).size());

		chunkedList = SimpleJdbcMapperUtils.chunkTheList(Arrays.asList(arr), 100);
		assertEquals(1, chunkedList.size());
		assertEquals(10, chunkedList.get(0).size());
	}

}
