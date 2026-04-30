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

import io.github.simplejdbcmapper.model.Employee;
import io.github.simplejdbcmapper.model.EmployeeSkill;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;
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
}
