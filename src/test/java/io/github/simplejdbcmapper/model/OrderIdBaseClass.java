package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;

public class OrderIdBaseClass {
	@Id(type = IdType.AUTO_GENERATED)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long orderId) {
		this.id = orderId;
	}

}
