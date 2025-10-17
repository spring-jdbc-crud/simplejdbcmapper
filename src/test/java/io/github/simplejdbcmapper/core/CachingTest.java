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
import io.github.simplejdbcmapper.model.Employee;
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
	void findByIdCache_test() {
		FindOperation fo = TestUtils.getFindOperation(sjm);
		SimpleCache<String, String> cache = fo.getFindByIdSqlCache();
		cache.clear();

		sjm.findById(Order.class, 1);
		assertEquals(1, cache.getSize());

		sjm.findById(Order.class, 2);
		assertEquals(1, cache.getSize());

		sjm.findById(Customer.class, 1);
		assertEquals(2, cache.getSize());
	}

	@Test
	void insertCache_test() {
		InsertOperation io = TestUtils.getInsertOperation(sjm);
		SimpleCache<String, SimpleJdbcInsert> cache = io.getInsertSqlCache();
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
	void updateCache_test() {
		UpdateOperation uo = TestUtils.getUpdateOperation(sjm);
		SimpleCache<String, SqlAndParams> cache = uo.getUpdateSqlCache();
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
	void updateSpecificPropertiesCache_test() {
		UpdateOperation uo = TestUtils.getUpdateOperation(sjm);
		SimpleCache<String, SqlAndParams> cache = uo.getUpdateSpecificPropertiesSqlCache();
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

	@Test
	void beanColumnsSqlCache_test() {
		FindOperation fo = TestUtils.getFindOperation(sjm);
		SimpleCache<String, String> cache = fo.getBeanColumnsSqlCache();
		cache.clear();

		sjm.getBeanFriendlySqlColumns(Customer.class);
		assertEquals(1, cache.getSize());

		sjm.getBeanFriendlySqlColumns(Customer.class);
		assertEquals(1, cache.getSize());

		sjm.getBeanFriendlySqlColumns(Product.class);
		assertEquals(2, cache.getSize());

		sjm.getBeanFriendlySqlColumns(Product.class);
		assertEquals(2, cache.getSize());

	}

	@Test
	void tableMappingCache_test() {
		SimpleJdbcMapperSupport sjms = TestUtils.getSimpleJdbcMapperSupport(sjm);
		SimpleCache<String, TableMapping> cache = sjms.getTableMappingCache();
		cache.clear();

		sjm.loadMapping(Customer.class);
		assertEquals(1, cache.getSize());

		sjm.loadMapping(Employee.class);
		assertEquals(2, cache.getSize());

		sjm.findById(Customer.class, 1);
		assertEquals(2, cache.getSize());

		sjm.loadMapping(Product.class);
		assertEquals(3, cache.getSize());

		sjm.findAll(Employee.class);
		assertEquals(3, cache.getSize());

	}

}
