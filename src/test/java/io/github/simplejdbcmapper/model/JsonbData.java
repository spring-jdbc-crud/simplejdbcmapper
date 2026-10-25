package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.core.ProductToJsonPostgresConverter;

@Table(name = "jsonb_data")
public class JsonbData {

	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@Column(name = "product_json", converter = ProductToJsonPostgresConverter.class)
	private Product product;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

}
