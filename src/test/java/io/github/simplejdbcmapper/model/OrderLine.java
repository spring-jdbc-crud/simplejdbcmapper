package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "order_line")
public class OrderLine {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer orderLineId;
	@Column
	private Long orderId;
	@Column
	private Integer productId;
	@Column
	private Integer numOfUnits;

	@Column
	private String lastMappedCol;

	private Order order;

	private Product product;

	private String status;

	public Integer getOrderLineId() {
		return orderLineId;
	}

	public void setOrderLineId(Integer id) {
		this.orderLineId = id;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getNumOfUnits() {
		return numOfUnits;
	}

	public void setNumOfUnits(Integer numOfUnits) {
		this.numOfUnits = numOfUnits;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLastMappedCol() {
		return lastMappedCol;
	}

	public void setLastMappedCol(String lastMappedCol) {
		this.lastMappedCol = lastMappedCol;
	}

}
