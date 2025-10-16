package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
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

	@Test
	void annotationOrderInheritedAudit_Test() {
		TableMapping tableMapping = sjm.getTableMapping(OrderInheritedAudit.class);
		List<String> mappedProperties = Arrays.asList("orderId", "orderDate", "customerId", "status", "createdOn",
				"createdBy", "updatedOn", "updatedBy", "version");
		for (String propertyName : mappedProperties) {
			assertNotNull(tableMapping.getPropertyMappingByPropertyName(propertyName));
		}
	}

	@Test
	void annotationOrderInheritedColumn_Test() {
		TableMapping tableMapping = sjm.getTableMapping(OrderInheritedColumn.class);
		List<String> mappedProperties = Arrays.asList("orderId", "orderDate", "customerId", "status");
		for (String propertyName : mappedProperties) {
			assertNotNull(tableMapping.getPropertyMappingByPropertyName(propertyName));
		}
	}

	@Test
	void annotationOrderInheritedId_Test() {
		TableMapping tableMapping = sjm.getTableMapping(OrderInheritedId.class);
		List<String> mappedProperties = Arrays.asList("orderId", "orderDate", "customerId", "status");
		for (String propertyName : mappedProperties) {
			assertNotNull(tableMapping.getPropertyMappingByPropertyName(propertyName));
		}
	}

	@Test
	void annotationOrderIdOverriden_failure_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.getTableMapping(OrderInheritedOverriddenId.class);
		});
		assertTrue(exception.getMessage().contains("@Id annotation not found in class"));

	}

	@Test
	void tableAnnotationInherited_Test() {
		Assertions.assertDoesNotThrow(() -> sjm.getTableMapping(ModelWithInheritedTableAnnotation.class));
	}

}
