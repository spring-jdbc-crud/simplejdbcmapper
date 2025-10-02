package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "employee_skill")
public class EmployeeSkill {

	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@Column
	private Integer employeeId;
	@Column
	private Integer skillId;

	public EmployeeSkill() {
	}

	public EmployeeSkill(Integer employeeId, Integer skillId) {
		this.employeeId = employeeId;
		this.skillId = skillId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public Integer getSkillId() {
		return skillId;
	}

	public void setSkillId(Integer skillId) {
		this.skillId = skillId;
	}
}
