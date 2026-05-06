package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.Employee;
import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.Product;
import io.github.simplejdbcmapper.relationship.RelationshipMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class RelationshipMapperTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void type_test() {
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);
		List<Product> products = sjm.findAll(Product.class);

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(OrderLine.class, orderLines, null);
		relMapper.addEntityResult(Product.class, products, null);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(null);
		});
		assertTrue(exception.getMessage().contains("type must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Employee.class);
		});
		assertTrue(exception.getMessage().contains("was not part of the query result set"));

	}

	@Test
	void addEntityResult_test() {
		RelationshipMapper relMapper = new RelationshipMapper();
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.addEntityResult(null, orderLines, null);
		});
		assertTrue(exception.getMessage().contains("entityType must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.addEntityResult(OrderLine.class, null, null);
		});
		assertTrue(exception.getMessage().contains("list must not be null"));

	}

}
