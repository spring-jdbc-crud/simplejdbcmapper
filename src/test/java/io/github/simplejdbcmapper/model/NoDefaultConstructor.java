package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "product")
public class NoDefaultConstructor {
	@Id
	private Integer id;

	public NoDefaultConstructor(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
