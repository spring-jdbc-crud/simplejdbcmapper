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
import io.github.simplejdbcmapper.model.Person;
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
		assertEquals(1, cache.size());

		sjm.findById(Order.class, 2);
		assertEquals(1, cache.size());

		sjm.findById(Customer.class, 1);
		assertEquals(2, cache.size());
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
		assertEquals(1, cache.size());

		sjm.delete(order);

		order = new Order();
		order.setOrderDate(LocalDateTime.now());
		order.setCustomerId(2);
		sjm.insert(order);
		assertEquals(1, cache.size());

		sjm.delete(order);

		Customer customer = new Customer();
		customer.setLastName("xyz");
		customer.setFirstName("abc");
		sjm.insert(customer);
		assertEquals(2, cache.size());
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
		assertEquals(1, cache.size());

		customer.setLastName("b");
		sjm.update(customer);
		assertEquals(1, cache.size());

		Product product = new Product();
		product.setProductId(10022);
		product.setName("xyz");
		sjm.insert(product);

		product.setName("abc");
		sjm.update(product);
		assertEquals(2, cache.size());

		product.setName("aaa");
		sjm.update(product);
		assertEquals(2, cache.size());

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
		assertEquals(1, cache.size());

		sjm.updateSpecificProperties(product, "cost");
		assertEquals(1, cache.size());

		sjm.updateSpecificProperties(product, "name");
		assertEquals(2, cache.size());

		sjm.updateSpecificProperties(product, "cost", "name");
		assertEquals(3, cache.size());

		sjm.updateSpecificProperties(product, "cost", "name");
		assertEquals(3, cache.size());

		product.setVersion(1);
		sjm.updateSpecificProperties(product, "cost", "name", "version");
		assertEquals(4, cache.size());

		// larger than CACHEABLE_UPDATE_PROPERTIES_COUNT = 5, so no caching
		product.setCreatedOn(LocalDateTime.now());
		sjm.updateSpecificProperties(product, "cost", "name", "version", "createdOn", "createdBy", "updatedOn");
		assertEquals(4, cache.size()); // no caching so count remains the same

		sjm.delete(product);

	}

	@Test
	void entityRowMapperSqlColumnsCache_test() {
		FindOperation fo = TestUtils.getFindOperation(sjm);
		SimpleCache<String, String> cache = fo.getEntitySqlColumnsCache();
		cache.clear();

		fo.getEntitySqlColumns(Customer.class);
		assertEquals(1, cache.size());

		fo.getEntitySqlColumns(Customer.class);
		assertEquals(1, cache.size());

		fo.getEntitySqlColumns(Product.class);
		assertEquals(2, cache.size());

		fo.getEntitySqlColumns(Product.class);
		assertEquals(2, cache.size());

	}

	@Test
	void entityRowMapperSqlColumnsAliasCache_test() {
		FindOperation fo = TestUtils.getFindOperation(sjm);
		SimpleCache<String, String> cache = fo.getEntitySqlColumnsAliasCache();
		cache.clear();

		fo.getEntitySqlColumns(Customer.class, "t1");
		assertEquals(1, cache.size());

		fo.getEntitySqlColumns(Customer.class, "t1");
		assertEquals(1, cache.size());

		fo.getEntitySqlColumns(Product.class, "p1");
		assertEquals(2, cache.size());

		fo.getEntitySqlColumns(Product.class, "p1");
		assertEquals(2, cache.size());

		fo.getEntitySqlColumns(Product.class, "p2");
		assertEquals(3, cache.size());

	}

	@Test
	void deleteSqlCache_test() {
		DeleteOperation op = TestUtils.getDeleteOperation(sjm);
		SimpleCache<String, String> cache = op.getDeleteSqlCache();
		cache.clear();

		Order ord = new Order();
		ord.setOrderId(801l);
		sjm.delete(ord);
		assertEquals(1, cache.size());

		sjm.deleteById(Order.class, 802);
		assertEquals(1, cache.size());

		Product prod = new Product();
		prod.setProductId(901);
		sjm.delete(prod);
		assertEquals(2, cache.size());

		sjm.deleteById(Product.class, 902);
		assertEquals(2, cache.size());

	}

	@Test
	void tableMappingCache_test() {
		SimpleJdbcMapperSupport sjms = TestUtils.getSimpleJdbcMapperSupport(sjm);
		SimpleCache<String, TableMapping> cache = sjms.getTableMappingCache();
		cache.clear();

		sjm.findById(Customer.class, 1);
		assertEquals(1, cache.size());

		Product prod = new Product();
		prod.setProductId(3121);
		prod.setName("xyz");
		sjm.insert(prod);
		assertEquals(2, cache.size());

		prod.setCost(10.25);
		sjm.update(prod);
		assertEquals(2, cache.size());

		prod.setCost(20.22);
		sjm.updateSpecificProperties(prod, "cost");
		assertEquals(2, cache.size());

		sjm.delete(prod);
		assertEquals(2, cache.size());

		Employee emp = new Employee();
		emp.setId(Integer.valueOf(1));
		emp.setLastName("xyz");
		sjm.update(emp);
		assertEquals(3, cache.size());

		Person person = new Person();
		person.setPersonId("person101");
		person.setFirstName("mike");
		sjm.updateSpecificProperties(person, "firstName");
		assertEquals(4, cache.size());

	}

	@Test
	void Cache_common_test() {
		SimpleCache<String, String> cache = new SimpleCache<>();
		cache.put("key", "value");
		cache.remove("key");
		assertEquals(0, cache.size());

	}

}
