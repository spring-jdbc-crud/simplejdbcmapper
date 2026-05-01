package io.github.simplejdbcmapper.core;

import java.util.List;

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

	@Test
	void OrderHasManyOrderLinesWhichHasOneProduct_success() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol").add(Product.class,
				"p");

		String sql = """
				SELECT %s
				FROM orders o
				LEFT JOIN order_line ol ON  o.id = ol.order_id
				LEFT JOIN product p ON ol.product_id = p.id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = resultListMap.getList(Order.class);
		List<OrderLine> orderLines = resultListMap.getList(OrderLine.class);
		List<Product> products = resultListMap.getList(Product.class);

		Relationship.mainList(orders).toManyList(orderLines).joinOn("id", "orderId").populate("orderLines");

		Relationship.mainList(orderLines).toOneList(products).joinOn("productId", "id").populate("product");

	}

	@Test
	void employeeHasManySkillsThroughIntermediateTable_success() {

		MultiEntity multiEntity = new MultiEntity().add(Employee.class, "emp").add(EmployeeSkill.class, "es")
				.add(Skill.class, "s");

		String sql = """
				SELECT %s
				FROM employee emp
				LEFT JOIN  employee_skill es ON emp.id = es.employee_id
				LEFT JOIN skill s ON es.skill_id = s.id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Employee> employees = resultListMap.getList(Employee.class);
		List<EmployeeSkill> employeeSkillList = resultListMap.getList(EmployeeSkill.class);
		List<Skill> skills = resultListMap.getList(Skill.class);

		Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
				.ids("id", "id").populate("skills");

	}

}
