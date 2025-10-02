package io.github.simplejdbcmapper.model;

import java.time.LocalDateTime;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.CreatedBy;
import io.github.simplejdbcmapper.annotation.CreatedOn;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.annotation.UpdatedBy;
import io.github.simplejdbcmapper.annotation.UpdatedOn;
import io.github.simplejdbcmapper.annotation.Version;

@Table(name = "product")
public class NonDefaultNamingProduct {

	@Column(name = "naME")
	private String productName;

	@Id
	@Column(name = "PRODUCT_id")
	private Integer id;

	@Column
	private Double cost;

	@CreatedOn
	@Column(name = "created_on")
	private LocalDateTime createdAt;

	@CreatedBy
	@Column(name = "created_by")
	private String whoCreated;

	@UpdatedOn
	@Column(name = "updated_on")
	private LocalDateTime updatedAt;

	@UpdatedBy
	@Column(name = "updated_by")
	private String whoUpdated;

	@Version
	@Column(name = "version")
	private Integer optiLock;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Double getCost() {
		return cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getWhoCreated() {
		return whoCreated;
	}

	public void setWhoCreated(String whoCreated) {
		this.whoCreated = whoCreated;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getWhoUpdated() {
		return whoUpdated;
	}

	public void setWhoUpdated(String whoUpdated) {
		this.whoUpdated = whoUpdated;
	}

	public Integer getOptiLock() {
		return optiLock;
	}

	public void setOptiLock(Integer optiLock) {
		this.optiLock = optiLock;
	}
}
