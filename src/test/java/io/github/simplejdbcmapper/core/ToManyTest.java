package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.Employee;
import io.github.simplejdbcmapper.model.EmployeeSkill;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.Product;
import io.github.simplejdbcmapper.model.Skill;
import io.github.simplejdbcmapper.relationship.Relationship;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ToManyTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void toMany_joinOn_validation_test() {
		List<Order> orders = sjm.findAll(Order.class);
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(orders).toManyList(orderLines).joinOn(null, "orderId");
		});
		assertTrue(exception.getMessage().contains("mainObjIdProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(orders).toManyList(orderLines).joinOn("orderId", null);
		});
		assertTrue(exception.getMessage().contains("relatedObjFkProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(orders).toManyList(orderLines).joinOn("x", "orderId");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(orders).toManyList(orderLines).joinOn("orderId", "x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

	}

	@Test
	void toMany_through_validation_test() {
		List<Employee> employees = sjm.findAll(Employee.class);
		List<Skill> skills = sjm.findAll(Skill.class);
		List<EmployeeSkill> employeeSkillList = sjm.findAll(EmployeeSkill.class);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, null, "skillId");
		});
		assertTrue(exception.getMessage().contains("fkPropertyToMainObjId must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", null);
		});
		assertTrue(exception.getMessage().contains("fkPropertyToRelatedObjId must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "x", "skillId");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

		assertDoesNotThrow(() -> {
			Relationship.mainList(employees).toManyList(skills).through(null, "x", "skillId");
		});

		assertDoesNotThrow(() -> {
			Relationship.mainList(employees).toManyList(skills).through(null, "employeeId", "x");
		});

	}

	@Test
	void toMany_ids_validation_test() {
		List<Employee> employees = sjm.findAll(Employee.class);
		List<Skill> skills = sjm.findAll(Skill.class);
		List<EmployeeSkill> employeeSkillList = sjm.findAll(EmployeeSkill.class);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids(null, "id");
		});
		assertTrue(exception.getMessage().contains("mainObjIdProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", null);
		});
		assertTrue(exception.getMessage().contains("relatedObjIdProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids("x", "id");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", "x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

		assertDoesNotThrow(() -> {
			Relationship.mainList(null).toManyList(skills).through(employeeSkillList, "employeeId", "skillId").ids("x",
					"id");
		});

		assertDoesNotThrow(() -> {
			Relationship.mainList(employees).toManyList(null).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", "x");
		});
	}

	@Test
	void toMany_populate_validation_test() {
		List<Employee> employees = sjm.findAll(Employee.class);
		List<Skill> skills = sjm.findAll(Skill.class);
		List<EmployeeSkill> employeeSkillList = sjm.findAll(EmployeeSkill.class);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", "id").populate(null);
		});
		assertTrue(exception.getMessage().contains("mainObjPropertyToPopulate must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", "id").populate("x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

		assertDoesNotThrow(() -> {
			Relationship.mainList(null).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", "id").populate("x");
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	void OrderHasManyOrderLines_success() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol");

		String sql = """
				SELECT %s
				FROM orders o
				LEFT JOIN order_line ol ON  o.id = ol.order_id
				WHERE o.id <= 4
				ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		@SuppressWarnings("rawtypes")
		Map<Class, List> resultMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = resultMap.get(Order.class);
		List<OrderLine> orderLines = resultMap.get(OrderLine.class);

		Relationship.mainList(orders).toManyList(orderLines).joinOn("id", "orderId").populate("orderLines");

		assertEquals(4, orders.size());
		assertEquals(2, orders.get(0).getOrderLines().size(), "ord 1 lines count failed");
		assertEquals(0, orders.get(2).getOrderLines().size(), "ord 3 lines count failed");
		assertEquals(1, orders.get(3).getOrderLines().size(), "ord 4 lines count failed");

		assertEquals(5, orders.get(0).getOrderLines().get(1).getNumOfUnits(), "ord 1 OrderLine 2 num_of_units failed");

		assertEquals(1, orders.get(1).getOrderLines().get(0).getNumOfUnits(), "ord 2 OrderLine 3 num_of_units failed");

	}

	@SuppressWarnings("unchecked")
	@Test
	void OrderHasManyOrderLinesWhichHasOneProduct_success() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol").add(Product.class,
				"p");

		String sql = """
				SELECT %s
				FROM orders o
				LEFT JOIN order_line ol ON  o.id = ol.order_id
				LEFT JOIN product p ON ol.product_id = p.id
				WHERE o.id <= 4
				ORDER BY o.id, ol.order_line_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		@SuppressWarnings("rawtypes")
		Map<Class, List> resultMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = resultMap.get(Order.class);
		List<OrderLine> orderLines = resultMap.get(OrderLine.class);
		List<Product> products = resultMap.get(Product.class);

		Relationship.mainList(orders).toManyList(orderLines).joinOn("id", "orderId").populate("orderLines");
		Relationship.mainList(orderLines).toOneList(products).joinOn("productId", "id").populate("product");

		assertEquals(4, orders.size());
		assertEquals(2, orders.get(0).getOrderLines().size(), "ord 1 lines count failed");
		assertEquals(0, orders.get(2).getOrderLines().size(), "ord 3 lines count failed");
		assertEquals(1, orders.get(3).getOrderLines().size(), "ord 4 lines count failed");

		assertNull(orders.get(3).getOrderLines().get(0).getProduct(),
				"ord 4 first orderline product should be null failed");

		assertEquals("shoes", orders.get(0).getOrderLines().get(0).getProduct().getName(),
				"ord 1 ordline 1 product name failed");
		assertEquals(4.55, orders.get(0).getOrderLines().get(1).getProduct().getCost(),
				"ord 1 ordLine 2 product cost ");

	}

	@SuppressWarnings("unchecked")
	@Test
	void employeeHasManySkillsThroughIntermediateTable_success() {

		MultiEntity multiEntity = new MultiEntity().add(Employee.class, "emp").add(EmployeeSkill.class, "es")
				.add(Skill.class, "s");

		String sql = """
				SELECT %s
				FROM employee emp
				LEFT JOIN  employee_skill es ON emp.id = es.employee_id
				LEFT JOIN skill s ON es.skill_id = s.id
				WHERE emp.id <= 4
				ORDER BY emp.id, s.id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		@SuppressWarnings("rawtypes")
		Map<Class, List> resultMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Employee> employees = resultMap.get(Employee.class);
		List<EmployeeSkill> employeeSkillList = resultMap.get(EmployeeSkill.class);
		List<Skill> skills = resultMap.get(Skill.class);

		Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
				.ids("id", "id").populate("skills");

		assertEquals(4, employees.size());
		assertEquals(2, employees.get(0).getSkills().size(), "emp 1 size failed");
		assertEquals(0, employees.get(1).getSkills().size(), "emp 2 size failed");
		assertEquals(3, employees.get(2).getSkills().size(), "emp 3 size failed");
		assertEquals(0, employees.get(3).getSkills().size(), "emp 4 size failed");

		assertEquals("java", employees.get(0).getSkills().get(0).getName(), "emp id 1 first skill failed");

		assertEquals("ruby", employees.get(2).getSkills().get(2).getName(), "emp id 2 last skill failed");

	}

}
