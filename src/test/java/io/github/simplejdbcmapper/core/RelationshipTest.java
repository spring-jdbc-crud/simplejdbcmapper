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
import io.github.simplejdbcmapper.model.EmployeeSkill;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.Product;
import io.github.simplejdbcmapper.model.Skill;
import io.github.simplejdbcmapper.relationship.RelationshipMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class RelationshipTest {

	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void toOne_relatedObj_test() {
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);
		List<Product> products = sjm.findAll(Product.class);

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(OrderLine.class, orderLines, "orderLineId");
		relMapper.addEntityResult(Product.class, products, "id");

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(OrderLine.class).toOne(null);
		});
		assertTrue(exception.getMessage().contains("relatedType must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(OrderLine.class).toOne(Employee.class);
		});
		assertTrue(exception.getMessage().contains("was not part of the query result set"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(OrderLine.class).toOne(OrderLine.class);
		});
		assertTrue(exception.getMessage().contains("mainType and relatedType cannot be same"));
	}

	@Test
	void toMany_relatedObj_test() {
		List<Order> orders = sjm.findAll(Order.class);
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(Order.class, orders, "id");
		relMapper.addEntityResult(OrderLine.class, orderLines, "orderLineId");

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(OrderLine.class).toMany(null);
		});
		assertTrue(exception.getMessage().contains("relatedType must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(OrderLine.class).toMany(Employee.class);
		});
		assertTrue(exception.getMessage().contains("was not part of the query result set"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Order.class).toMany(Order.class);
		});
		assertTrue(exception.getMessage().contains("mainType and relatedType cannot be same"));

	}

	@Test
	void toManyThrough_intermediateType_test() {
		List<Employee> employees = sjm.findAll(Employee.class);
		List<Skill> skills = sjm.findAll(Skill.class);
		List<EmployeeSkill> employeeSkillList = sjm.findAll(EmployeeSkill.class);

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(Employee.class, employees, "id");
		relMapper.addEntityResult(Skill.class, skills, "id");
		relMapper.addEntityResult(EmployeeSkill.class, employeeSkillList, "id");

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Employee.class).toMany(Skill.class).through(null, "employeeId", "skillId");
		});
		assertTrue(exception.getMessage().contains("throughType must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.type(Employee.class).toMany(Skill.class).through(Order.class, "employeeId", "skillId");
		});
		assertTrue(exception.getMessage().contains("was not part of the query result set"));

	}

}
