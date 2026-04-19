package io.github.simplejdbcmapper.model;

public class NoDefaultConstructor {
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
