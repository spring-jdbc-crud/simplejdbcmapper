package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "product")
public class NoMatchingColumn {
	@Id
	private Integer idSomething;

	@Column
	private Double cost;

	public Integer getIdSomething() {
		return idSomething;
	}

	public void setIdSomething(Integer idSomething) {
		this.idSomething = idSomething;
	}

	public Double getCost() {
		return cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

}
