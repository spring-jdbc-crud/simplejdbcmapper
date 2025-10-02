package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.Testsynonym;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class OracleTableSynonymTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	@Qualifier("allSimpleJdbcMapper")
	private SimpleJdbcMapper sjm;

	@BeforeEach
	void beforeMethod() {
		// tests will only run if oracle
		if (!jdbcDriver.contains("oracle")) {
			Assumptions.assumeTrue(false);
		}
	}

	@Test
	void oracleSynonymTable_test() {
		// testsynonym table created in SCHEMA1 and synonym created in SCHEMA2 and
		// accessing it in SCHEMA2
		Testsynonym model = new Testsynonym();
		model.setName("abc");

		sjm.insert(model);
		assertNotNull(model.getId());

		model.setName("xyz");
		sjm.update(model);

		Testsynonym model2 = sjm.findById(Testsynonym.class, model.getId());
		assertEquals("xyz", model2.getName());
	}

}
