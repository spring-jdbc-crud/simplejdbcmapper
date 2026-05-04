package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import io.github.simplejdbcmapper.model.Skill;
import io.github.simplejdbcmapper.relationship.Relationship;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ToManyThroughTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void toManythrough_through_validation_test() {
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
		assertTrue(exception.getMessage().contains("Invalid argument. Could not find a getter for"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Could not find a getter for"));

		assertDoesNotThrow(() -> {
			Relationship.mainList(employees).toManyList(skills).through(null, "x", "skillId");
		});

		assertDoesNotThrow(() -> {
			Relationship.mainList(employees).toManyList(skills).through(null, "employeeId", "x");
		});

	}

	@Test
	void toManythrough_ids_validation_test() {
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
		assertTrue(exception.getMessage().contains("Invalid argument. Could not find a getter for"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", "x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Could not find a getter for"));

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
	void toManythrough_populate_validation_test() {
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

		// type mismatch ie property exits but of different type
		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", "id").populate("lastName");
		});
		assertTrue(exception.getMessage().contains("argument type mismatch"));

	}

	@Test
	void employeeToManySkillsThroughIntermediateTable_success() {

		MultiEntity multiEntity = new MultiEntity().add(Employee.class, "emp").add(EmployeeSkill.class, "es")
				.add(Skill.class, "s");

		String sql = """
				SELECT %s
				FROM employee emp
				LEFT JOIN  employee_skill es ON emp.id = es.employee_id
				LEFT JOIN skill s ON es.skill_id = s.id
				WHERE emp.id <= 4
				ORDER BY emp.id, s.name
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Employee> employees = resultListMap.getList(Employee.class);
		List<EmployeeSkill> employeeSkillList = resultListMap.getList(EmployeeSkill.class);
		List<Skill> skills = resultListMap.getList(Skill.class);

		Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
				.ids("id", "id").populate("skills");

		assertEquals(4, employees.size());
		assertEquals(2, employees.get(0).getSkills().size(), "emp 1 size failed");
		assertEquals(0, employees.get(1).getSkills().size(), "emp 2 size failed");
		assertEquals(3, employees.get(2).getSkills().size(), "emp 3 size failed");
		assertEquals(0, employees.get(3).getSkills().size(), "emp 4 size failed");

		assertEquals("java", employees.get(0).getSkills().get(0).getName(), "emp id 1 first skill failed");

		assertEquals("typescript", employees.get(2).getSkills().get(2).getName(), "emp id 2 last skill failed");

	}

	@Test
	void toManyThrough_null_entries_test() {

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

		ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Employee> employees = resultListMap.getList(Employee.class);
		employees.add(1, null);
		List<EmployeeSkill> employeeSkillList = resultListMap.getList(EmployeeSkill.class);
		employeeSkillList.add(0, null);

		List<Skill> skills = resultListMap.getList(Skill.class);
		skills.add(2, null);

		Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
				.ids("id", "id").populate("skills");

		assertEquals("ruby", employees.get(3).getSkills().get(2).getName());

	}

	@Test
	void ToManyThrough_no_propertyValues_entry_test() {

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

		ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Employee> employees = resultListMap.getList(Employee.class);
		employees.add(1, new Employee());
		List<EmployeeSkill> employeeSkillList = resultListMap.getList(EmployeeSkill.class);
		employeeSkillList.add(0, new EmployeeSkill());

		List<Skill> skills = resultListMap.getList(Skill.class);
		skills.add(2, new Skill());

		Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
				.ids("id", "id").populate("skills");

		assertEquals("ruby", employees.get(3).getSkills().get(2).getName());

	}

	@Test
	void ToManyThrough_null_lists_test() {

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

		ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Employee> employees = resultListMap.getList(Employee.class);
		List<EmployeeSkill> employeeSkillList = resultListMap.getList(EmployeeSkill.class);
		List<Skill> skills = resultListMap.getList(Skill.class);

		assertDoesNotThrow(() -> {
			Relationship.mainList(null).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", "id").populate("skills");
		});

		assertDoesNotThrow(() -> {
			Relationship.mainList(employees).toManyList(null).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", "id").populate("skills");
		});

		assertDoesNotThrow(() -> {
			Relationship.mainList(employees).toManyList(skills).through(null, "employeeId", "skillId").ids("id", "id")
					.populate("skills");
		});

	}

	@Test
	void ToManyThrough_NoRecords_test() {

		MultiEntity multiEntity = new MultiEntity().add(Employee.class, "emp").add(EmployeeSkill.class, "es")
				.add(Skill.class, "s");

		String sql = """
				SELECT %s
				FROM employee emp
				LEFT JOIN  employee_skill es ON emp.id = es.employee_id
				LEFT JOIN skill s ON es.skill_id = s.id
				WHERE emp.id < 0
				ORDER BY emp.id, s.id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Employee> employees = resultListMap.getList(Employee.class);
		List<EmployeeSkill> employeeSkillList = resultListMap.getList(EmployeeSkill.class);
		List<Skill> skills = resultListMap.getList(Skill.class);

		assertEquals(0, employees.size());
		assertEquals(0, employeeSkillList.size());
		assertEquals(0, skills.size());

		assertDoesNotThrow(() -> {
			Relationship.mainList(employees).toManyList(skills).through(employeeSkillList, "employeeId", "skillId")
					.ids("id", "id").populate("skills");
		});

	}
}
