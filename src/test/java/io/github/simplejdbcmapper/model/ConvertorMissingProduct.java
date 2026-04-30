package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "product")
public class ConvertorMissingProduct {
	@Id
	private Integer id;

	@Column
	private char[] name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public char[] getName() {
		return name;
	}

	public void setName(char[] name) {
		this.name = name;
	}

}
