package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;

public class OrderLineOrderIdInteger {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer orderLineId;
	@Column
	private Integer orderId;
	@Column
	private Integer productId;
	@Column
	private Integer numOfUnits;

	@Column
	private String lastMappedCol;

	private Order order;

	private Product product;

	public Integer getOrderLineId() {
		return orderLineId;
	}

	public void setOrderLineId(Integer orderLineId) {
		this.orderLineId = orderLineId;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
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

	public String getLastMappedCol() {
		return lastMappedCol;
	}

	public void setLastMappedCol(String lastMappedCol) {
		this.lastMappedCol = lastMappedCol;
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

}
