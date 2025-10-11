package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.Customer;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.Product;
import io.github.simplejdbcmapper.model.ProductWithNoAuditFields;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class CachingTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void jtm_findByIdCache_test() {
		SimpleCache<String, String> cache = sjm.getFindByIdSqlCache();
		cache.clear();

		sjm.findById(Order.class, 1);
		assertEquals(1, cache.getSize());

		sjm.findById(Order.class, 2);
		assertEquals(1, cache.getSize());

		sjm.findById(Customer.class, 1);
		assertEquals(2, cache.getSize());
	}

	@Test
	void jtm_insertCache_test() {
		SimpleCache<String, SimpleJdbcInsert> cache = sjm.getInsertSqlCache();
		cache.clear();

		Order order = new Order();
		order.setOrderDate(LocalDateTime.now());
		order.setCustomerId(2);

		sjm.insert(order);
		assertEquals(1, cache.getSize());

		sjm.delete(order);

		order = new Order();
		order.setOrderDate(LocalDateTime.now());
		order.setCustomerId(2);
		sjm.insert(order);
		assertEquals(1, cache.getSize());

		sjm.delete(order);

		Customer customer = new Customer();
		customer.setLastName("xyz");
		customer.setFirstName("abc");
		sjm.insert(customer);
		assertEquals(2, cache.getSize());
		sjm.delete(customer);
	}

	@Test
	void jtm_updateCache_test() {
		SimpleCache<String, SqlAndParams> cache = sjm.getUpdateSqlCache();
		cache.clear();

		Customer customer = new Customer();
		customer.setLastName("xyz");
		customer.setFirstName("abc");
		sjm.insert(customer);

		customer.setLastName("a");
		sjm.update(customer);
		assertEquals(1, cache.getSize());

		customer.setLastName("b");
		sjm.update(customer);
		assertEquals(1, cache.getSize());

		Product product = new Product();
		product.setProductId(10022);
		product.setName("xyz");
		sjm.insert(product);

		product.setName("abc");
		sjm.update(product);
		assertEquals(2, cache.getSize());

		product.setName("aaa");
		sjm.update(product);
		assertEquals(2, cache.getSize());

		sjm.delete(customer);

		sjm.delete(product);

	}

	@Test
	void jtm_updatePropertiesCache_test() {
		SimpleCache<String, SqlAndParams> cache = sjm.getUpdateSpecificPropertiesSqlCache();
		cache.clear();

		ProductWithNoAuditFields product = new ProductWithNoAuditFields();
		product.setProductId(811);
		product.setName("p-811");
		product.setCost(4.75);
		sjm.insert(product);

		sjm.updateSpecificProperties(product, "cost");
		assertEquals(1, cache.getSize());

		sjm.updateSpecificProperties(product, "cost");
		assertEquals(1, cache.getSize());

		sjm.updateSpecificProperties(product, "name");
		assertEquals(2, cache.getSize());

		sjm.updateSpecificProperties(product, "cost", "name");
		assertEquals(3, cache.getSize());

		sjm.updateSpecificProperties(product, "cost", "name");
		assertEquals(3, cache.getSize());

		product.setVersion(1);
		sjm.updateSpecificProperties(product, "cost", "name", "version");
		assertEquals(4, cache.getSize());

		// larger than CACHEABLE_UPDATE_PROPERTIES_COUNT = 3, so no caching
		product.setCreatedOn(LocalDateTime.now());
		sjm.updateSpecificProperties(product, "cost", "name", "version", "createdOn");
		assertEquals(4, cache.getSize()); // no caching so count remains the same

		sjm.delete(product);

	}

}
