package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.Customer;
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

	@Test
	void close_Test() {
		SimpleJdbcMapperSupport sjms = TestUtils.getSimpleJdbcMapperSupport(sjm);
		SimpleCache<String, TableMapping> tableMappingCache = sjms.getTableMappingCache();
		tableMappingCache.clear();

		FindOperation fo = TestUtils.getFindOperation(sjm);
		SimpleCache<String, String> findCache = fo.getFindByIdSqlCache();
		findCache.clear();

		SimpleCache<String, String> rawColCache = fo.getRawColumnsSqlCache();
		rawColCache.clear();

		InsertOperation io = TestUtils.getInsertOperation(sjm);
		SimpleCache<String, SimpleJdbcInsert> insertCache = io.getInsertSqlCache();
		insertCache.clear();

		UpdateOperation uo = TestUtils.getUpdateOperation(sjm);
		SimpleCache<String, SqlAndParams> updateCache = uo.getUpdateSqlCache();
		updateCache.clear();

		SimpleCache<String, SqlAndParams> updateSpecCache = uo.getUpdateSpecificPropertiesSqlCache();
		updateSpecCache.clear();

		DeleteOperation dop = TestUtils.getDeleteOperation(sjm);
		SimpleCache<String, String> delCache = dop.getDeleteSqlCache();
		delCache.clear();

		sjm.findById(Customer.class, 1);
		assertTrue(findCache.size() > 0);
		assertTrue(rawColCache.size() > 0);

		Product prod = new Product();
		prod.setProductId(3121);
		prod.setName("xyz");
		sjm.insert(prod);
		assertTrue(insertCache.size() > 0);

		prod.setCost(10.25);
		sjm.update(prod);
		assertTrue(updateCache.size() > 0);

		prod.setCost(20.22);
		sjm.updateSpecificProperties(prod, "cost");
		assertTrue(updateSpecCache.size() > 0);

		sjm.delete(prod);
		assertTrue(delCache.size() > 0);

		// now close and check the cache sizes are 0 ie we have cleared all references
		// in sjm
		sjm.close();

		assertEquals(0, findCache.size());
		assertEquals(0, rawColCache.size());
		assertEquals(0, insertCache.size());
		assertEquals(0, updateCache.size());
		assertEquals(0, updateSpecCache.size());
		assertEquals(0, delCache.size());

	}

}
