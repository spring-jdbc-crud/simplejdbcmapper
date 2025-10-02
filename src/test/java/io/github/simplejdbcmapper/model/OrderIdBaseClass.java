package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;

public class OrderIdBaseClass {
	@Id(type = IdType.AUTO_GENERATED)
	private Long orderId;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

}
