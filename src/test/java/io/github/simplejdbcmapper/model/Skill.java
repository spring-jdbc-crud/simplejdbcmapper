package io.github.simplejdbcmapper.model;

import java.util.ArrayList;
import java.util.List;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "skill")
public class Skill {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@Column
	private String name;

	private List<Employee> employees = new ArrayList<>();

	public Skill() {
	}

	public Skill(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(List<Employee> employees) {
		this.employees = employees;
	}
}
