package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;

public class OrderColumnBaseClass {
	@Column
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
