package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.model.ConvertorMissingProduct;
import io.github.simplejdbcmapper.model.Customer;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.PersonView;
import io.github.simplejdbcmapper.model.Product;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class FindTest {
	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void findById_Test() {
		Order order = sjm.findById(Order.class, 1);

		assertNotNull(order.getOrderId());
		assertNotNull(order.getOrderDate());
		assertNotNull(order.getCreatedBy());
		assertNotNull(order.getCreatedOn());
		assertNotNull(order.getUpdatedBy());
		assertNotNull(order.getUpdatedOn());
		assertNotNull(order.getVersion());

	}

	@Test
	void findById_idNull_Test() {
		Order order = sjm.findById(Order.class, null);

		assertNull(order);
	}

	@Test
	void findById_databaseView_Test() {
		PersonView pv = sjm.findById(PersonView.class, "person101");

		assertNotNull(pv.getPersonId());
		assertNotNull(pv.getFirstName());
		assertNotNull(pv.getLastName());
	}

	@Test
	void findAll_Test() {
		List<Order> orders = sjm.findAll(Order.class);
		assertTrue(orders.size() >= 2);

		for (int idx = 0; idx < orders.size(); idx++) {
			assertNotNull(orders.get(idx).getOrderId());
			assertNotNull(orders.get(idx).getOrderDate());
		}
	}

	@Test
	void findAll_WithOrderBy_success_Test() {
		List<Order> orders = sjm.findAll(Order.class, new SortBy("status"));
		assertTrue(orders.size() >= 2);
		for (int idx = 0; idx < orders.size(); idx++) {
			assertNotNull(orders.get(idx).getOrderId());
			assertNotNull(orders.get(idx).getOrderDate());
		}

		orders = sjm.findAll(Order.class, new SortBy("status", "ASC"));
		assertTrue(orders.size() >= 2);
		for (int idx = 0; idx < orders.size(); idx++) {
			assertNotNull(orders.get(idx).getOrderId());
			assertNotNull(orders.get(idx).getOrderDate());
		}

		orders = sjm.findAll(Order.class, new SortBy("status", "DESC"));
		assertTrue(orders.size() >= 2);
		for (int idx = 0; idx < orders.size(); idx++) {
			assertNotNull(orders.get(idx).getOrderId());
			assertNotNull(orders.get(idx).getOrderDate());
		}

		orders = sjm.findAll(Order.class, new SortBy("status"), new SortBy("orderId"));
		assertTrue(orders.size() >= 2);
		for (int idx = 0; idx < orders.size(); idx++) {
			assertNotNull(orders.get(idx).getOrderId());
			assertNotNull(orders.get(idx).getOrderDate());
		}

		orders = sjm.findAll(Order.class, new SortBy("status"), new SortBy("orderId", "DESC"));
		assertTrue(orders.size() >= 2);
		for (int idx = 0; idx < orders.size(); idx++) {
			assertNotNull(orders.get(idx).getOrderId());
			assertNotNull(orders.get(idx).getOrderDate());
		}

		orders = sjm.findAll(Order.class, new SortBy("status", "ASC"), new SortBy("orderId", "DESC"));
		assertTrue(orders.size() >= 2);
		for (int idx = 0; idx < orders.size(); idx++) {
			assertNotNull(orders.get(idx).getOrderId());
			assertNotNull(orders.get(idx).getOrderDate());
		}

	}

	@Test
	void findAll_WithOrderBy_failure_Test() {
		SortBy sb = new SortBy("xyz");
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.findAll(Order.class, sb);
		});
		assertTrue(exception.getMessage().contains("is not a mapped property for class"));
	}

	@Test
	void findByPropertyValue_sucess_Test() {
		List<OrderLine> orderLines = sjm.findByPropertyValue(OrderLine.class, "orderId", 1);
		assertEquals(2, orderLines.size());
	}

	@Test
	void findByPropertyValue_OrderBy_success_Test() {
		List<OrderLine> orderLines = sjm.findByPropertyValue(OrderLine.class, "orderId", 1, new SortBy("productId"));
		assertEquals(2, orderLines.size());

		orderLines = sjm.findByPropertyValue(OrderLine.class, "orderId", 1, new SortBy("productId", "asc"),
				new SortBy("numOfUnits", "deSC"));
		assertEquals(2, orderLines.size());
	}

	@Test
	void findByPropertyValue_WithOrderBy_failure_Test() {
		SortBy sortBy = new SortBy("xyz");
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.findByPropertyValue(Order.class, "orderId", 1, sortBy);
		});
		assertTrue(exception.getMessage().contains("is not a mapped property for class"));
	}

	@Test
	void findByPropertyValue_nullPropertyName_Test() {
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.findByPropertyValue(OrderLine.class, null, 1);
		});
		assertTrue(exception.getMessage().contains("propertyName must not be null"));
	}

	@Test
	void findByPropertyValue_nullClazz_Test() {
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.findByPropertyValue(null, "orderId", 1);
		});
		assertTrue(exception.getMessage().contains("entityType must not be null"));
	}

	@Test
	void findByPropertyValue_nullPropertyValue_Test() {
		List<Customer> list = sjm.findByPropertyValue(Customer.class, "firstName", null);
		assertEquals(1, list.size());
	}

	@Test
	void findByPropertyValue_InvalidProperty_Test() {
		Exception exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.findByPropertyValue(OrderLine.class, "x", 1);
		});
		assertTrue(exception.getMessage().contains("does not have a mapping"));
	}

	@Test
	void findByPropertyValues_success_Test() {
		Integer[] orderIds = { 1, 2, 3 };
		List<Order> orders = sjm.findByPropertyValues(Order.class, "orderId", Arrays.asList(orderIds));
		assertEquals(3, orders.size());
	}

	@Test
	void findByPropertyValues_withSortBy_success_Test() {
		Integer[] orderIds = { 1, 2, 3 };
		List<Order> orders = sjm.findByPropertyValues(Order.class, "orderId", Arrays.asList(orderIds),
				new SortBy("orderId", "DESC"));
		assertEquals(3, orders.size());

		orders = sjm.findByPropertyValues(Order.class, "orderId", Arrays.asList(orderIds),
				new SortBy("orderId", "DESC"), new SortBy("status", "ASC"));
		assertEquals(3, orders.size());
	}

	@Test
	void findByPropertyValues_WithOrderBy_failure_Test() {
		Integer[] orderIds = { 1, 2, 3 };
		SortBy sb1 = new SortBy("orderId");
		SortBy sb2 = new SortBy("xyz");
		List<Integer> list = Arrays.asList(orderIds);
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.findByPropertyValues(Order.class, "orderId", list, sb1, sb2);
		});
		assertTrue(exception.getMessage().contains("is not a mapped property for class"));
	}

	@Test
	void findByPropertyValues_nullPropertyName_Test() {
		Set<Integer> set = new HashSet<>();
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.findByPropertyValues(OrderLine.class, null, set);
		});
		assertTrue(exception.getMessage().contains("propertyName must not be null"));
	}

	@Test
	void findByPropertyValues_nullPropertyValues_Test() {
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.findByPropertyValues(OrderLine.class, "orderId", null);
		});
		assertTrue(exception.getMessage().contains("propertyValues must not be null"));
	}

	@Test
	void findByPropertyValues_emptyPropertyValues_Test() {
		List<OrderLine> lines = sjm.findByPropertyValues(OrderLine.class, "orderId", new ArrayList<Integer>());
		assertEquals(0, lines.size());
	}

	@Test
	void findByPropertyValues_ofWhichSomeAreNull_Test() {
		String[] descriptions = { null, "some description", null };
		List<Product> products = sjm.findByPropertyValues(Product.class, "description",
				new HashSet<String>(Arrays.asList(descriptions)));
		assertTrue(products.stream().filter(e -> "some description".equals(e.getDescription())).count() > 0);
		assertTrue(products.stream().filter(e -> e.getDescription() == null).count() > 0);
	}

	@Test
	void findByPropertyValues_ofWhichHasOnlyNull_Test() {
		String[] descriptions = { null };
		List<Product> products = sjm.findByPropertyValues(Product.class, "description", Arrays.asList(descriptions));
		assertTrue(products.size() > 0);
	}

	@Test
	void findByPropertyValuesInvalidProperty_Test() {
		Integer[] orderIds = { 1, 2, 3 };

		List<Integer> list = Arrays.asList(orderIds);
		Exception exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.findByPropertyValues(Order.class, "x", list);
		});
		assertTrue(exception.getMessage().contains("does not have a mapping"));
	}

	@Test
	void find_ConvertorMissing_Test() {
		Exception exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.findById(ConvertorMissingProduct.class, 1);
		});
		assertTrue(exception.getMessage().contains("could not convert ResultSet value"));
	}

}
