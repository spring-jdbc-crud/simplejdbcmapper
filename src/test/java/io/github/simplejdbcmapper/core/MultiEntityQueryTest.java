package io.github.simplejdbcmapper.core;

import java.util.List;
import java.util.Map;

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

@SpringBootTest
@ExtendWith(SpringExtension.class)
class MultiEntityQueryTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@SuppressWarnings("unchecked")
	@Test
	void OrderHasManyOrderLinesWhichHasOneProduct_success() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol").add(Product.class,
				"p");

		String sql = """
				SELECT %s
				FROM orders o
				JOIN order_line ol ON ol.order_id = o.order_id
				JOIN product p ON p.product_id = ol.product_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		@SuppressWarnings("rawtypes")
		Map<Class, List> resultMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = resultMap.get(Order.class);
		List<OrderLine> orderLines = resultMap.get(OrderLine.class);
		List<Product> products = resultMap.get(Product.class);

		SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId", "orderId", "orderLines");
		SimpleJdbcMapperUtils.populateHasOne(orderLines, products, "productId", "productId", "product");

	}

	@SuppressWarnings("unchecked")
	@Test
	void employeeHasManySkillsThroughAssociativeTable_success() {

		MultiEntity multiEntity = new MultiEntity().add(Employee.class, "emp").add(EmployeeSkill.class, "es")
				.add(Skill.class, "s");

		String sql = """
				SELECT %s
				FROM employee emp
				LEFT JOIN  employee_skill es ON es.employee_id = emp.id
				LEFT JOIN skill s ON s.id = es.skill_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		@SuppressWarnings("rawtypes")
		Map<Class, List> resultMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Employee> employees = resultMap.get(Employee.class);
		List<EmployeeSkill> employeeSkillList = resultMap.get(EmployeeSkill.class);
		List<Skill> skills = resultMap.get(Skill.class);

		SimpleJdbcMapperUtils.populateHasManyThrough(employees, skills, "id", "id",
				new AssociativeJoiner(employeeSkillList, "employeeId", "skillId"), "skills");

	}

}
