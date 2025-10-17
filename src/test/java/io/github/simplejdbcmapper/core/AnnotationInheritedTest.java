package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.exception.AnnotationException;
import io.github.simplejdbcmapper.model.ModelWithInheritedTableAnnotation;
import io.github.simplejdbcmapper.model.OrderInheritedAudit;
import io.github.simplejdbcmapper.model.OrderInheritedColumn;
import io.github.simplejdbcmapper.model.OrderInheritedId;
import io.github.simplejdbcmapper.model.OrderInheritedOverriddenId;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class AnnotationInheritedTest {

	@Autowired
	private SimpleJdbcMapper sjm;

	private TableMappingHelper tmh;

	private SimpleJdbcMapperSupport sjmSupport;

	@BeforeEach
	void beforeMethod() {
		tmh = TestUtils.getTableMappingHelper(sjm);
		sjmSupport = TestUtils.getSimpleJdbcMapperSupport(sjm);
	}

	@Test
	void annotationOrderInheritedAuditProperty_Test() {
		TableMapping tableMapping = tmh.getTableMapping(OrderInheritedAudit.class);
		List<String> mappedProperties = Arrays.asList("orderId", "orderDate", "customerId", "status", "createdOn",
				"createdBy", "updatedOn", "updatedBy", "version");
		for (String propertyName : mappedProperties) {
			assertNotNull(tableMapping.getPropertyMappingByPropertyName(propertyName));
		}
	}

	@Test
	void annotationOrderInheritedAudit_Test() {

		OrderInheritedAudit obj = new OrderInheritedAudit();
		obj.setOrderDate(LocalDateTime.now());
		obj.setCustomerId(2);

		sjm.insert(obj);

		// check if auto assigned properties have been assigned.
		if (sjmSupport.getRecordAuditedBySupplier() != null) {
			assertEquals("tester", obj.getCreatedBy());
			assertEquals("tester", obj.getUpdatedBy());
		}
		if (sjmSupport.getRecordAuditedOnSupplier() != null) {
			assertNotNull(obj.getCreatedOn());
			assertNotNull(obj.getUpdatedOn());
		}
		assertEquals(1, obj.getVersion());

		OrderInheritedAudit obj2 = sjm.findById(OrderInheritedAudit.class, obj.getOrderId());
		assertNotNull(obj2.getOrderId());
		assertNotNull(obj2.getOrderDate());
		if (sjmSupport.getRecordAuditedBySupplier() != null) {
			assertEquals("tester", obj.getCreatedBy());
			assertEquals("tester", obj.getUpdatedBy());
		}
		if (sjmSupport.getRecordAuditedOnSupplier() != null) {
			assertNotNull(obj.getCreatedOn());
			assertNotNull(obj.getUpdatedOn());
		}
		assertEquals(1, obj2.getVersion());
	}

	@Test
	void annotationOrderInheritedColumn_Test() {
		TableMapping tableMapping = tmh.getTableMapping(OrderInheritedColumn.class);
		List<String> mappedProperties = Arrays.asList("orderId", "orderDate", "customerId", "status");
		for (String propertyName : mappedProperties) {
			assertNotNull(tableMapping.getPropertyMappingByPropertyName(propertyName));
		}
	}

	@Test
	void annotationOrderInheritedId_Test() {
		TableMapping tableMapping = tmh.getTableMapping(OrderInheritedId.class);
		List<String> mappedProperties = Arrays.asList("orderId", "orderDate", "customerId", "status");
		for (String propertyName : mappedProperties) {
			assertNotNull(tableMapping.getPropertyMappingByPropertyName(propertyName));
		}
	}

	@Test
	void annotationOrderIdOverriden_failure_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			tmh.getTableMapping(OrderInheritedOverriddenId.class);
		});
		assertTrue(exception.getMessage().contains("@Id annotation not found in class"));

	}

	@Test
	void tableAnnotationInherited_Test() {
		Assertions.assertDoesNotThrow(() -> tmh.getTableMapping(ModelWithInheritedTableAnnotation.class));
	}

}
