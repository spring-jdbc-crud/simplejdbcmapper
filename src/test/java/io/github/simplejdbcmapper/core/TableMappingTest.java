package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.CompanyCatalogSchema2;
import io.github.simplejdbcmapper.model.CompanySchema2;
import io.github.simplejdbcmapper.model.CustomerCatalogSchema1;
import io.github.simplejdbcmapper.model.CustomerSchema1;
import io.github.simplejdbcmapper.model.Order;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TableMappingTest {
	@Autowired
	private DataSource dataSource;

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Test
	void schema_name_from_simplejdbcmapper_config_test() {
		if (!jdbcDriver.contains("mysql")) {
			SimpleJdbcMapper sjm = new SimpleJdbcMapper(dataSource, "schema1");
			sjm.loadMapping(CustomerSchema1.class);

			TableMappingHelper tmHelper = TestUtils.getTableMappingHelper(sjm);

			TableMapping tm = tmHelper.getTableMapping(Order.class);
			assertEquals("schema1", tm.getSchemaName());
		}
	}

	@Test
	void schema_name_from_table_annotation_test() {
		if (!jdbcDriver.contains("mysql") && !jdbcDriver.contains("oracle")) {
			SimpleJdbcMapper sjm = new SimpleJdbcMapper(dataSource);
			sjm.loadMapping(CustomerSchema1.class);

			TableMappingHelper tmHelper = TestUtils.getTableMappingHelper(sjm);

			TableMapping tm = tmHelper.getTableMapping(CustomerSchema1.class);
			assertEquals("schema1", tm.getSchemaName());
		}
	}

	@Test
	void schema_name_overriddenby_table_annotation_test() {
		if (!jdbcDriver.contains("mysql")) {
			SimpleJdbcMapper sjm = new SimpleJdbcMapper(dataSource, "schema1");
			sjm.loadMapping(CustomerSchema1.class);

			TableMappingHelper tmHelper = TestUtils.getTableMappingHelper(sjm);

			TableMapping tm = tmHelper.getTableMapping(CompanySchema2.class);
			assertEquals("SCHEMA2", tm.getSchemaName());
		}
	}

	@Test
	void catalog_name_from_simplejdbcmapper_config_test() {
		if (jdbcDriver.contains("mysql")) {
			SimpleJdbcMapper sjm = new SimpleJdbcMapper(dataSource, null, "schema1");
			sjm.loadMapping(Order.class);

			TableMappingHelper tmHelper = TestUtils.getTableMappingHelper(sjm);
			TableMapping tm = tmHelper.getTableMapping(Order.class);
			assertEquals("schema1", tm.getCatalogName());
		}
	}

	@Test
	void catalog_name_from_table_annotation_test() {
		if (jdbcDriver.contains("mysql")) {
			SimpleJdbcMapper sjm = new SimpleJdbcMapper(dataSource);
			sjm.loadMapping(CustomerCatalogSchema1.class);
			TableMappingHelper tmHelper = TestUtils.getTableMappingHelper(sjm);
			TableMapping tm = tmHelper.getTableMapping(CustomerCatalogSchema1.class);
			assertEquals("schema1", tm.getCatalogName());
		}
	}

	@Test
	void catalog_name_overriddenby_table_annotation_test() {
		if (jdbcDriver.contains("mysql")) {
			SimpleJdbcMapper sjm = new SimpleJdbcMapper(dataSource, null, "schema1");
			sjm.loadMapping(CompanyCatalogSchema2.class);

			TableMappingHelper tmHelper = TestUtils.getTableMappingHelper(sjm);
			TableMapping tm = tmHelper.getTableMapping(CompanyCatalogSchema2.class);
			assertEquals("schema2", tm.getCatalogName());
		}
	}

}
