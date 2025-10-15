package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.NonDefaultNamingProduct;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class NonDefaultNamingModelTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void findById_Test() {
		NonDefaultNamingProduct prod = sjm.findById(NonDefaultNamingProduct.class, 1);

		assertEquals(1, prod.getId());
		assertEquals("shoes", prod.getProductName());
		assertEquals("system", prod.getWhoCreated());
		assertEquals("system", prod.getWhoUpdated());
		assertEquals(1, prod.getOptiLock());
		assertNotNull(prod.getCreatedAt());
		assertNotNull(prod.getUpdatedAt());
	}

	@Test
	void findAll_Test() {
		List<NonDefaultNamingProduct> list = sjm.findAll(NonDefaultNamingProduct.class);

		NonDefaultNamingProduct prod = list.get(0);

		assertNotNull(prod.getProductName());
	}

	@Test
	void insert_Test() {
		NonDefaultNamingProduct prod = new NonDefaultNamingProduct();
		prod.setId(1005);
		prod.setProductName("hat");
		prod.setCost(12.25);

		sjm.insert(prod);

		NonDefaultNamingProduct prod2 = sjm.findById(NonDefaultNamingProduct.class, 1005);

		assertEquals(1005, prod2.getId());
		assertEquals("hat", prod2.getProductName());
		assertEquals(12.25, prod2.getCost());
		assertEquals(1, prod2.getOptiLock());
	}

	@Test
	void update_Test() {
		NonDefaultNamingProduct product = new NonDefaultNamingProduct();
		product.setId(1010);
		product.setProductName("hat");
		product.setCost(12.25);

		sjm.insert(product);

		NonDefaultNamingProduct prod1 = sjm.findById(NonDefaultNamingProduct.class, product.getId());

		prod1.setProductName("cap");
		sjm.update(prod1);

		NonDefaultNamingProduct prod2 = sjm.findById(NonDefaultNamingProduct.class, prod1.getId());

		assertEquals(1010, prod2.getId());
		assertEquals("cap", prod2.getProductName());
		assertTrue(product.getOptiLock() < prod2.getOptiLock());
	}

}
