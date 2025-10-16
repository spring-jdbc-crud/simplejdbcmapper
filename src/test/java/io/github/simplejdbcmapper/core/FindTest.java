package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.PersonView;

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

}
