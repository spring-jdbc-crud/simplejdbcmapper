package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.Customer;
import io.github.simplejdbcmapper.model.Employee;
import io.github.simplejdbcmapper.model.EmployeeSkill;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.Product;
import io.github.simplejdbcmapper.model.Skill;
import io.github.simplejdbcmapper.relationship.Relationship;
import io.github.simplejdbcmapper.relationship.RelationshipMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class RelationshipMapperTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void type_test() {
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);
		List<Product> products = sjm.findAll(Product.class);

		RelationshipMapper relMapper = new RelationshipMapper();
		assertDoesNotThrow(() -> {
			relMapper.addEntityResult(OrderLine.class, orderLines, "orderLineId");
		});

		assertDoesNotThrow(() -> {
			relMapper.addEntityResult(Product.class, products, "id");
		});

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.addEntityResult(Product.class, products, "id");
		});
		assertTrue(exception.getMessage().contains("duplicate entityType"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(null);
		});
		assertTrue(exception.getMessage().contains("type must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Employee.class);
		});
		assertTrue(exception.getMessage().contains("was not part of the query results"));

	}

	@Test
	void addEntityResult_test() {
		RelationshipMapper relMapper = new RelationshipMapper();
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.addEntityResult(null, orderLines, "id");
		});
		assertTrue(exception.getMessage().contains("entityType must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.addEntityResult(OrderLine.class, null, "id");
		});
		assertTrue(exception.getMessage().contains("list must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.addEntityResult(OrderLine.class, orderLines, null);
		});
		assertTrue(exception.getMessage().contains("idPropertyName must not be null"));

	}

	@Test
	void assemble_success_test() {
		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol").add(Product.class,
				"p");

		String sql = """
						SELECT %s
						FROM orders o
						LEFT JOIN order_line ol ON o.id = ol.order_id
						LEFT JOIN product p ON ol.product_id = p.id
						WHERE o.id <= 4 ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		Relationship orderLineToOneProduct = Relationship.type(OrderLine.class).toOne(Product.class)
				.joinOn("productId", "id").populate("product");

		Relationship orderToManyOrderLine = Relationship.type(Order.class).toMany(OrderLine.class)
				.joinOn("id", "orderId").populate("orderLines");

		assertDoesNotThrow(() -> {
			relMapper.assemble(orderToManyOrderLine, orderLineToOneProduct);
		});
	}

	@Test
	void assemble_validation_test() {
		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol")
				.add(Product.class, "p").add(Customer.class, "c");

		String sql = """
						SELECT %s
						FROM orders o
						LEFT JOIN customer c ON o.customer_id = c.id
						LEFT JOIN order_line ol ON o.id = ol.order_id
						LEFT JOIN product p ON ol.product_id = p.id
						WHERE o.id <= 4 ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		Relationship orderLineToOneProduct = Relationship.type(OrderLine.class).toOne(Product.class)
				.joinOn("productId", "id").populate("product");

		Relationship orderToManyOrderLine = Relationship.type(Order.class).toMany(OrderLine.class)
				.joinOn("id", "orderId").populate("orderLines");

		Relationship orderToOneCustomer = Relationship.type(Order.class).toOne(Customer.class)
				.joinOn("customerId", "id").populate("customer");

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.assemble();
		});
		assertTrue(exception.getMessage().contains("relationships array must not be empty"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.assemble(null);
		});
		assertTrue(exception.getMessage().contains("relationships must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.assemble(orderLineToOneProduct, orderLineToOneProduct);
		});
		assertTrue(exception.getMessage().contains("Duplicate relationship"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.assemble(orderToManyOrderLine, orderToManyOrderLine);
		});
		assertTrue(exception.getMessage().contains("Duplicate relationship"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.assemble(orderLineToOneProduct, null, orderToManyOrderLine);
		});
		assertTrue(exception.getMessage().contains("relationship must not be null"));

		Relationship empToManySkills = Relationship.type(Employee.class).toMany(Skill.class)
				.through(EmployeeSkill.class, "employeeId", "skillId").populate("skills");

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.assemble(orderLineToOneProduct, empToManySkills);
		});
		assertTrue(exception.getMessage().contains("was not part of the query result"));

		Assertions.assertDoesNotThrow(() -> relMapper.assemble(orderToManyOrderLine, orderToOneCustomer));

	}

}
