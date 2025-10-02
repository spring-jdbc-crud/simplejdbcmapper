package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.OrderInheritedAudit;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class SimpleJdbcMapperSupportTest {
	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void inheritedClass_fieldCount_test() {
		List<Field> fields = sjm.getSimpleJdbcMapperSupport().getAllFields(OrderInheritedAudit.class);

		assertEquals(9, fields.size());
	}
}
