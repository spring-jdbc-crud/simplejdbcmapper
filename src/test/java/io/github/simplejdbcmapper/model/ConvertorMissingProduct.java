package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "product")
public class ConvertorMissingProduct {
	@Id
	private Integer productId;

	@Column
	private char[] name;

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public char[] getName() {
		return name;
	}

	public void setName(char[] name) {
		this.name = name;
	}

}
