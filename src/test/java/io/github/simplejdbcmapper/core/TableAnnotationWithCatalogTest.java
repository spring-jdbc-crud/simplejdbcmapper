package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.CustomerCatalogSchema1;
import io.github.simplejdbcmapper.model.PersonCatalogSchema1;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TableAnnotationWithCatalogTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	@Qualifier("allSimpleJdbcMapper")
	private SimpleJdbcMapper sjm;

	@Test
	void autoId_crud_withCatalogSchema1Annotation() {
		if (jdbcDriver.contains("mysql")) {
			CustomerCatalogSchema1 customer = new CustomerCatalogSchema1();
			customer.setFirstName("john");
			customer.setLastName("doe");

			sjm.insert(customer);

			customer.setLastName("xyz");
			sjm.update(customer);

			CustomerCatalogSchema1 customer1 = sjm.findById(CustomerCatalogSchema1.class, customer.getCustomerId());

			assertNotNull(customer1);
			assertEquals("xyz", customer1.getLastName());

			int count = sjm.delete(customer1);
			assertEquals(1, count);
		}
	}

	@Test
	void nonAutoId_crud_withCatalogSchema1Annotation() {
		if (jdbcDriver.contains("mysql")) {
			PersonCatalogSchema1 person = new PersonCatalogSchema1();
			person.setPersonId("2001");
			person.setFirstName("john");
			person.setLastName("doe");

			sjm.insert(person);

			person.setLastName("xyz");
			sjm.update(person);

			PersonCatalogSchema1 person1 = sjm.findById(PersonCatalogSchema1.class, person.getPersonId());
			assertNotNull(person1);
			assertEquals("xyz", person1.getLastName());

			int count = sjm.delete(person1);
			assertEquals(1, count);
		}
	}

}
