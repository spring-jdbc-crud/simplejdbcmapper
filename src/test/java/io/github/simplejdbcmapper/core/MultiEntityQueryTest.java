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
import io.github.simplejdbcmapper.relationship.Relationship;

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
				LEFT JOIN order_line ol ON  o.order_id = ol.order_id
				LEFT JOIN product p ON ol.product_id = p.product_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		@SuppressWarnings("rawtypes")
		Map<Class, List> resultMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = resultMap.get(Order.class);
		List<OrderLine> orderLines = resultMap.get(OrderLine.class);
		List<Product> products = resultMap.get(Product.class);

		// SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId",
		// "orderId", "orderLines");

		Relationship.mainList(orders).toManyList(orderLines).joinOn("orderId", "orderId").populate("orderLines");

		Relationship.mainList(orderLines).toOneList(products).joinOn("productId", "productId").populate("product");
		// Relationship.type(Order.class).hasOne(Product.class).join(sql,
		// sql).populate(sql)

		// SimpleJdbcMapperUtils.populateHasOne(orderLines, products, "productId",
		// "productId", "product");

	}

	@SuppressWarnings("unchecked")
	@Test
	void employeeHasManySkillsThroughAssociativeTable_success() {

		MultiEntity multiEntity = new MultiEntity().add(Employee.class, "emp").add(EmployeeSkill.class, "es")
				.add(Skill.class, "s");

		String sql = """
				SELECT %s
				FROM employee emp
				LEFT JOIN  employee_skill es ON emp.id = es.employee_id
				LEFT JOIN skill s ON es.skill_id = s.id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		@SuppressWarnings("rawtypes")
		Map<Class, List> resultMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Employee> employees = resultMap.get(Employee.class);
		List<EmployeeSkill> employeeSkillList = resultMap.get(EmployeeSkill.class);
		List<Skill> skills = resultMap.get(Skill.class);

		SimpleJdbcMapperUtils.populateHasManyThrough(employees, skills, "id", "id",
				new IntermediateJoiner(employeeSkillList, "employeeId", "skillId"), "skills");

	}

}
