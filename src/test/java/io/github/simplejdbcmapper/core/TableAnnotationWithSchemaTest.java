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

import io.github.simplejdbcmapper.model.CustomerSchema1;
import io.github.simplejdbcmapper.model.PersonSchema1;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TableAnnotationWithSchemaTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	@Qualifier("allSimpleJdbcMapper")
	private SimpleJdbcMapper sjm;

	@Test
	void autoId_crud_withSchema1Annotation() {
		if (!jdbcDriver.contains("mysql")) {
			CustomerSchema1 customer = new CustomerSchema1();
			customer.setFirstName("john");
			customer.setLastName("doe");

			sjm.insert(customer);

			customer.setLastName("xyz");
			sjm.update(customer);

			CustomerSchema1 customer1 = sjm.findById(CustomerSchema1.class, customer.getCustomerId());

			assertNotNull(customer1);
			assertEquals("xyz", customer1.getLastName());

			int count = sjm.delete(customer1);
			assertEquals(1, count);
		}
	}

	@Test
	void nonAutoId_crud_withSchema1Annotation() {
		if (!jdbcDriver.contains("mysql")) {
			PersonSchema1 person = new PersonSchema1();
			person.setPersonId("2001");
			person.setFirstName("john");
			person.setLastName("doe");

			sjm.insert(person);

			person.setLastName("xyz");
			sjm.update(person);

			PersonSchema1 person1 = sjm.findById(PersonSchema1.class, person.getPersonId());
			assertNotNull(person1);
			assertEquals("xyz", person1.getLastName());

			int count = sjm.delete(person1);
			assertEquals(1, count);
		}
	}

}
