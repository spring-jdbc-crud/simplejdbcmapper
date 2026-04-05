package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.exception.AnnotationException;
import io.github.simplejdbcmapper.model.NoTableAnnotationModel;
import io.github.simplejdbcmapper.model.NonDefaultNamingProduct;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.Product;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class SimpleJdbcMapperTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void loadMapping_success_Test() {
		Assertions.assertDoesNotThrow(() -> {
			sjm.loadMapping(Order.class);
		});
	}

	@Test
	void loadMapping_failure_Test() {
		Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.loadMapping(NoTableAnnotationModel.class);
		});
	}

	@Test
	void getBeanFriendlySqlColumns_test() {
		NonDefaultNamingProduct p = new NonDefaultNamingProduct();
		p.setId(9812);
		p.setProductName("test");
		p.setCost(10.25);
		sjm.insert(p);

		String sql = "SELECT " + sjm.getBeanFriendlySqlColumns(NonDefaultNamingProduct.class)
				+ " FROM product WHERE name = ?";

		// Using JdbcClient api for the above sql
		List<NonDefaultNamingProduct> products = sjm.getJdbcClient().sql(sql).param("test")
				.query(NonDefaultNamingProduct.class).list();

		assertEquals(1, products.size());
		assertEquals(10.25, products.get(0).getCost());
		assertEquals("test", products.get(0).getProductName());

		// Using JdbcTemplate api for the above sql
		List<NonDefaultNamingProduct> products2 = sjm.getJdbcTemplate().query(sql,
				BeanPropertyRowMapper.newInstance(NonDefaultNamingProduct.class), "test");

		assertEquals(1, products2.size());
		assertEquals(10.25, products2.get(0).getCost());
		assertEquals("test", products2.get(0).getProductName());
	}

	@Test
	void setRecordAuditedBySupplier_resetting_failure() {
		SimpleJdbcMapper m = new SimpleJdbcMapper(dataSource);
		Supplier<String> supplier = () -> "tester";
		m.setRecordAuditedBySupplier(supplier);
		Assertions.assertThrows(IllegalStateException.class, () -> {
			m.setRecordAuditedBySupplier(supplier);
		});
	}

	@Test
	void setRecordAuditedOnSupplier_resetting_failure() {
		SimpleJdbcMapper m = new SimpleJdbcMapper(dataSource);
		Supplier<LocalDateTime> supplier = () -> LocalDateTime.now();
		m.setRecordAuditedOnSupplier(supplier);
		Assertions.assertThrows(IllegalStateException.class, () -> {
			m.setRecordAuditedOnSupplier(supplier);
		});
	}

	@Test
	void setConversionService_resetting_failure() {
		SimpleJdbcMapper m = new SimpleJdbcMapper(dataSource);
		DefaultConversionService dcs = new DefaultConversionService();
		m.setConversionService(dcs);
		Assertions.assertThrows(IllegalStateException.class, () -> {
			m.setConversionService(dcs);
		});
	}

	@Test
	void getSchemaName_test() {
		if (jdbcDriver.contains("oracle")) {
			SimpleJdbcMapper sjm = new SimpleJdbcMapper(dataSource, "schema1");
			assertEquals("schema1", sjm.getSchemaName());
		}
	}

	@Test
	void getCatalogName_test() {
		if (jdbcDriver.contains("mysql")) {
			SimpleJdbcMapper sjm = new SimpleJdbcMapper(dataSource, null, "schema1");
			assertEquals("schema1", sjm.getCatalogName());
		}
	}

	@Test
	void getConversionService_test() {
		assertTrue(sjm.getConversionService() != null);
	}

	@Test
	void getNamedParameterJdbcTemplate_test() {
		assertTrue(sjm.getNamedParameterJdbcTemplate() != null);
	}

	@Test
	void getPropertyToColumnMappings_Test() {
		Map<String, String> map = sjm.getPropertyToColumnMappings(Product.class);
		assertEquals(9, map.size());
		assertTrue(map.containsKey("productId"));
	}

}
