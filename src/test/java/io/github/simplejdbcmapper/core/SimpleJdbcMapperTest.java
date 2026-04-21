package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import io.github.simplejdbcmapper.model.NonDefaultNamingProduct;
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
	void getBeanFriendlySqlColumns_test() {
		NonDefaultNamingProduct p = new NonDefaultNamingProduct();
		p.setId(9812);
		p.setProductName("test9812");
		p.setCost(10.25);
		sjm.insert(p);

		String sql = "SELECT " + sjm.getBeanFriendlySqlColumns(NonDefaultNamingProduct.class)
				+ " FROM product WHERE name = ?";

		// Using JdbcClient api for the above sql
		List<NonDefaultNamingProduct> products = sjm.getJdbcClient().sql(sql).param("test9812")
				.query(NonDefaultNamingProduct.class).list();

		assertEquals(1, products.size());
		assertEquals(10.25, products.get(0).getCost());
		assertEquals("test9812", products.get(0).getProductName());

		// Using JdbcTemplate api for the above sql
		List<NonDefaultNamingProduct> products2 = sjm.getJdbcTemplate().query(sql,
				BeanPropertyRowMapper.newInstance(NonDefaultNamingProduct.class), "test9812");

		assertEquals(1, products2.size());
		assertEquals(10.25, products2.get(0).getCost());
		assertEquals("test9812", products2.get(0).getProductName());
	}

	@Test
	void getBeanFriendlySqlColumns_withTableAlias_test() {
		NonDefaultNamingProduct p = new NonDefaultNamingProduct();
		p.setId(9900);
		p.setProductName("test9900");
		p.setCost(10.25);
		sjm.insert(p);

		String sql = "SELECT " + sjm.getBeanFriendlySqlColumns(NonDefaultNamingProduct.class, "t1")
				+ " FROM product t1 WHERE t1.name = ?";

		// Using JdbcClient api for the above sql
		List<NonDefaultNamingProduct> products = sjm.getJdbcClient().sql(sql).param("test9900")
				.query(NonDefaultNamingProduct.class).list();

		assertEquals(1, products.size());
		assertEquals(10.25, products.get(0).getCost());
		assertEquals("test9900", products.get(0).getProductName());

		// Using JdbcTemplate api for the above sql
		List<NonDefaultNamingProduct> products2 = sjm.getJdbcTemplate().query(sql,
				BeanPropertyRowMapper.newInstance(NonDefaultNamingProduct.class), "test9900");

		assertEquals(1, products2.size());
		assertEquals(10.25, products2.get(0).getCost());
		assertEquals("test9900", products2.get(0).getProductName());
	}

	@Test
	void getBeanFriendlySqlColumns_IllegalArgs_test() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.getBeanFriendlySqlColumns(null);
		});

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.getBeanFriendlySqlColumns(null, "t1");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.getBeanFriendlySqlColumns(Product.class, null);
		});

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.getBeanFriendlySqlColumns(Product.class, "   ");
		});
	}

	@Test
	void getEntityRowMapperSqlColumns_test() {
		NonDefaultNamingProduct p = new NonDefaultNamingProduct();
		p.setId(5461);
		p.setProductName("test5461");
		p.setCost(10.25);
		sjm.insert(p);

		String sql = "SELECT " + sjm.getEntityRowMapperSqlColumns(NonDefaultNamingProduct.class)
				+ " FROM product WHERE name = ?";

		// Using JdbcClient api for the above sql
		List<NonDefaultNamingProduct> products = sjm.getJdbcClient().sql(sql).param("test5461")
				.query(sjm.getEntityRowMapper(NonDefaultNamingProduct.class)).list();

		assertEquals(1, products.size());
		assertEquals(10.25, products.get(0).getCost());
		assertEquals("test5461", products.get(0).getProductName());

		// Using JdbcTemplate api for the above sql
		List<NonDefaultNamingProduct> products2 = sjm.getJdbcTemplate().query(sql,
				sjm.getEntityRowMapper(NonDefaultNamingProduct.class), "test5461");

		assertEquals(1, products2.size());
		assertEquals(10.25, products2.get(0).getCost());
		assertEquals("test5461", products2.get(0).getProductName());
	}

	@Test
	void getEntityRowMapperSqlColumns_IllegalArgs_test() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.getEntityRowMapperSqlColumns(null);
		});

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.getEntityRowMapperSqlColumns(null, "t1");
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.getEntityRowMapperSqlColumns(Product.class, null);
		});

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			sjm.getEntityRowMapperSqlColumns(Product.class, "   ");
		});
	}

	@Test
	void getEntityRowMapperSqlColumns_withTableAlias_test() {
		NonDefaultNamingProduct p = new NonDefaultNamingProduct();
		p.setId(7190);
		p.setProductName("test7190");
		p.setCost(10.25);
		sjm.insert(p);

		String sql = "SELECT " + sjm.getEntityRowMapperSqlColumns(NonDefaultNamingProduct.class, "t1")
				+ " FROM product t1 WHERE t1.name = ?";

		// Using JdbcClient api for the above sql
		List<NonDefaultNamingProduct> products = sjm.getJdbcClient().sql(sql).param("test7190")
				.query(sjm.getEntityRowMapper(NonDefaultNamingProduct.class)).list();

		assertEquals(1, products.size());
		assertEquals(10.25, products.get(0).getCost());
		assertEquals("test7190", products.get(0).getProductName());

		// Using JdbcTemplate api for the above sql
		List<NonDefaultNamingProduct> products2 = sjm.getJdbcTemplate().query(sql,
				sjm.getEntityRowMapper(NonDefaultNamingProduct.class), "test7190");

		assertEquals(1, products2.size());
		assertEquals(10.25, products2.get(0).getCost());
		assertEquals("test7190", products2.get(0).getProductName());
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
		Supplier<LocalDateTime> supplier = LocalDateTime::now;
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
			SimpleJdbcMapper sjm1 = new SimpleJdbcMapper(dataSource, "schema1");
			assertEquals("schema1", sjm1.getSchemaName());
		}
	}

	@Test
	void getCatalogName_test() {
		if (jdbcDriver.contains("mysql")) {
			SimpleJdbcMapper sjm1 = new SimpleJdbcMapper(dataSource, null, "schema1");
			assertEquals("schema1", sjm1.getCatalogName());
		}
	}

	@Test
	void getConversionService_test() {
		SimpleJdbcMapper sjm1 = new SimpleJdbcMapper(dataSource);
		assertNotNull(sjm1.getConversionService());
	}

	@Test
	void getNamedParameterJdbcTemplate_test() {
		assertNotNull(sjm.getNamedParameterJdbcTemplate());
	}

	@Test
	void getPropertyToColumnMappings_Test() {
		Map<String, String> map = sjm.getPropertyToColumnMappings(Product.class);
		assertEquals(9, map.size());
		assertTrue(map.containsKey("productId"));
	}

	// Order of tests causing problem. Address it another day. It works when run on
	// its own
	// @Test
	void close_Test() {
		SimpleJdbcMapperSupport sjms = TestUtils.getSimpleJdbcMapperSupport(sjm);
		FindOperation fo = TestUtils.getFindOperation(sjm);
		InsertOperation io = TestUtils.getInsertOperation(sjm);
		UpdateOperation uo = TestUtils.getUpdateOperation(sjm);
		DeleteOperation dop = TestUtils.getDeleteOperation(sjm);

		sjm.close();

		assertNull(sjms.getTableMappingCache());
		assertNull(fo.getFindByIdSqlCache());
		assertNull(fo.getEntityRowMapperSqlColumnsCache());
		assertNull(fo.getEntityRowMapperSqlColumnsAliasCache());
		assertNull(io.getInsertSqlCache());
		assertNull(uo.getUpdateSqlCache());
		assertNull(uo.getUpdateSpecificPropertiesSqlCache());
		assertNull(dop.getDeleteSqlCache());

		assertNull(sjms.getRecordAuditedOnSupplier());
		assertNull(sjms.getRecordAuditedBySupplier());
		assertNull(sjms.getConversionService());

		assertNull(sjms.getJdbcClient());
		assertNull(sjms.getJdbcTemplate());
		assertNull(sjms.getNamedParameterJdbcTemplate());
		assertNull(sjms.getDataSource());

	}

}
