package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.exception.AnnotationException;
import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.exception.OptimisticLockingException;
import io.github.simplejdbcmapper.model.Customer;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.Person;
import io.github.simplejdbcmapper.model.PersonWithPrimitiveId;
import io.github.simplejdbcmapper.model.Product;
import io.github.simplejdbcmapper.model.ProductWithNoAuditFields;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class UpdateTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	private SimpleJdbcMapperSupport sjmSupport;

	@BeforeEach
	void beforeMethod() {
		// clear caches to force a table meta data lookup from database.
		sjmSupport = TestUtils.getSimpleJdbcMapperSupport(sjm);
	}

	@Test
	void update_Test() throws Exception {
		Order order = sjm.findById(Order.class, 1);
		LocalDateTime prevUpdatedOn = order.getUpdatedOn();

		Thread.sleep(1000); // provide interval so timestamps end up different

		order.setStatus("COMPLETE");
		sjm.update(order);

		// check if auto assigned properties have changed.
		assertEquals(2, order.getVersion());
		if (sjmSupport.getRecordAuditedBySupplier() != null) {
			assertEquals("tester", order.getUpdatedBy());
		}
		if (sjmSupport.getRecordAuditedOnSupplier() != null) {
			assertNotNull(order.getUpdatedOn());
			assertTrue(order.getUpdatedOn().isAfter(prevUpdatedOn));
		}

		// requery and check
		order = sjm.findById(Order.class, 1);
		assertEquals("COMPLETE", order.getStatus());
		assertEquals(2, order.getVersion()); // version incremented
		if (sjmSupport.getRecordAuditedBySupplier() != null) {
			assertEquals("tester", order.getUpdatedBy());
		}
		if (sjmSupport.getRecordAuditedOnSupplier() != null) {
			assertNotNull(order.getUpdatedOn());
			assertTrue(order.getUpdatedOn().isAfter(prevUpdatedOn));
		}

		// reset status for later tests to work. Refactor
		order.setStatus("IN PROCESS");
		sjm.update(order);
	}

	@Test
	void update_withIdOfTypeInteger_Test() {
		Product product = sjm.findById(Product.class, 6);
		Product product1 = sjm.findById(Product.class, 6);
		product1.setName("xyz");
		sjm.update(product1);

		Product product2 = sjm.findById(Product.class, 6);

		assertEquals("xyz", product1.getName());
		assertTrue(product2.getVersion() > product.getVersion()); // version incremented
	}

	@Test
	void update_withIdOfTypeString_Test() {
		Person person = sjm.findById(Person.class, "person101");
		person.setLastName("new name");
		sjm.update(person);

		Person person1 = sjm.findById(Person.class, "person101");

		assertEquals("new name", person1.getLastName());
	}

	@Test
	void insert_withIdPrimitiveFailure_Test() {
		// id is int. insert should fail.
		PersonWithPrimitiveId person = new PersonWithPrimitiveId();
		person.setLastName("john");
		person.setFirstName("doe");

		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.insert(person);
		});

		assertTrue(exception.getMessage().contains("is an id and cannot be a primitive type"));
	}

	@Test
	void update_throwsOptimisticLockingException_Test() {
		Order order = sjm.findById(Order.class, 2);
		order.setVersion(order.getVersion() - 1);
		Assertions.assertThrows(OptimisticLockingException.class, () -> {
			sjm.update(order);
		});
	}

	@Test
	void update_withNullVersion_Test() {
		Order order = sjm.findById(Order.class, 2);
		order.setVersion(null);
		Exception exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.update(order);
		});

		assertTrue(exception.getMessage().contains("must not be null when updating"));

	}

	@Test
	void update_nullObjectFailure_Test() {
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.update(null);
		});
		assertTrue(exception.getMessage().contains("Object must not be null"));
	}

	@Test
	void update_nullIdFailure_Test() {
		Customer customer = sjm.findById(Customer.class, 1);
		customer.setCustomerId(null);
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.update(customer);
		});
		assertTrue(exception.getMessage().contains("is the id and must not be null"));
	}

	@Test
	void update_nonDatabaseProperty_Test() {
		Person person = sjm.findById(Person.class, "person101");
		person.setSomeNonDatabaseProperty("xyz");
		sjm.update(person);

		// requery
		Person person2 = sjm.findById(Person.class, "person101");

		assertNotNull(person2);
		assertNull(person2.getSomeNonDatabaseProperty());
	}

	@Test
	void updateSpecificProperties_IdAndAutoAssign_failure() {
		Order order = sjm.findById(Order.class, 1);

		Exception exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.updateSpecificProperties(order, "orderId");
		});
		assertTrue(exception.getMessage().contains("cannot be updated"));

		exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.updateSpecificProperties(order, "createdOn"); // @CreatedOn auto assign
		});
		assertTrue(exception.getMessage().contains("are not properties that can be specifically updated"));

		exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.updateSpecificProperties(order, "createdBy"); // @CreatedBy auto assign
		});
		assertTrue(exception.getMessage().contains("are not properties that can be specifically updated"));

		exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.updateSpecificProperties(order, "updatedOn"); // @UpdatedOn auto assign
		});
		assertTrue(exception.getMessage().contains("are not properties that can be specifically updated"));

		exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.updateSpecificProperties(order, "updatedBy"); // @UpdatedBy auto assign
		});
		assertTrue(exception.getMessage().contains("are not properties that can be specifically updated"));

		exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.updateSpecificProperties(order, "version"); // @Version auto assign
		});
		assertTrue(exception.getMessage().contains("are not properties that can be specifically updated"));

	}

	@Test
	void updateSpecificProperties_invalidProperty_failure() {
		Order order = sjm.findById(Order.class, 1);
		Exception exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.updateSpecificProperties(order, "xyz");
		});
		assertTrue(exception.getMessage().contains("No mapping found for property"));

		exception = Assertions.assertThrows(MapperException.class, () -> {
			sjm.updateSpecificProperties(order, "status", null);
		});
		assertTrue(exception.getMessage().contains("No mapping found for property"));

	}

	@Test
	void updateSpecificProperties_success() throws Exception {
		Customer customer = sjm.findById(Customer.class, 5);

		customer.setLastName("bbb");
		customer.setFirstName("aaa");
		sjm.updateSpecificProperties(customer, "lastName", "firstName");

		customer = sjm.findById(Customer.class, customer.getCustomerId());
		assertEquals("bbb", customer.getLastName());
		assertEquals("aaa", customer.getFirstName());

		Order order = new Order();
		order.setStatus("PENDING");
		sjm.insert(order);

		LocalDateTime prevUpdatedOn = order.getUpdatedOn();

		Thread.sleep(1000); // avoid timing issue.

		order.setStatus("DONE");
		sjm.updateSpecificProperties(order, "status");

		assertEquals("DONE", order.getStatus());
		// check if auto assigned properties have changed.
		assertEquals(2, order.getVersion());
		if (sjmSupport.getRecordAuditedOnSupplier() != null) {
			assertTrue(order.getUpdatedOn().isAfter(prevUpdatedOn));
		}
		if (sjmSupport.getRecordAuditedBySupplier() != null) {
			assertEquals("tester", order.getUpdatedBy());
		}
		if (sjmSupport.getRecordAuditedOnSupplier() != null) {
			assertNotNull(order.getUpdatedOn());
		}

		sjm.delete(order);

	}

	@Test
	void updateSpecificProperties_propertiesCountLargerThanCacheableSize_success() {
		ProductWithNoAuditFields product = new ProductWithNoAuditFields();
		product.setProductId(801);
		product.setName("p-801");
		product.setCost(4.75);
		sjm.insert(product);

		// number of properties larger than CACHEABLE_UPDATE_PROPERTIES_COUNT. Just
		// want to make sure it works.
		product.setVersion(1);
		product.setCreatedOn(LocalDateTime.now());
		sjm.updateSpecificProperties(product, "cost", "name", "version", "createdOn");

		ProductWithNoAuditFields productWithNoAuditFields = sjm.findById(ProductWithNoAuditFields.class,
				product.getProductId());
		assertEquals(1, productWithNoAuditFields.getVersion());
		assertNotNull(productWithNoAuditFields.getCreatedOn());

		sjm.delete(product);
	}
}
