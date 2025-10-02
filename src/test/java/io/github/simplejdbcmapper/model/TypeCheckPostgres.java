package io.github.simplejdbcmapper.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "type_check")
public class TypeCheckPostgres {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@Column
	private LocalDate localDateData;

	@Column
	private java.util.Date javaUtilDateData;

	@Column
	private LocalDateTime localDateTimeData;

	@Column
	private java.util.Date javaUtilDateTsData;

	@Column
	private BigDecimal bigDecimalData;

	@Column
	private Boolean booleanVal;

	@Column
	private byte[] image;

	@Column
	private UUID someUuid;

	@Column(name = "string_enum")
	private StatusEnum status;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LocalDate getLocalDateData() {
		return localDateData;
	}

	public void setLocalDateData(LocalDate localDateData) {
		this.localDateData = localDateData;
	}

	public java.util.Date getJavaUtilDateData() {
		return javaUtilDateData;
	}

	public void setJavaUtilDateData(java.util.Date javaUtilDateData) {
		this.javaUtilDateData = javaUtilDateData;
	}

	public LocalDateTime getLocalDateTimeData() {
		return localDateTimeData;
	}

	public void setLocalDateTimeData(LocalDateTime localDateTimeData) {
		this.localDateTimeData = localDateTimeData;
	}

	public java.util.Date getJavaUtilDateTsData() {
		return javaUtilDateTsData;
	}

	public void setJavaUtilDateTsData(java.util.Date javaUtilDateTsData) {
		this.javaUtilDateTsData = javaUtilDateTsData;
	}

	public BigDecimal getBigDecimalData() {
		return bigDecimalData;
	}

	public void setBigDecimalData(BigDecimal bigDecimalData) {
		this.bigDecimalData = bigDecimalData;
	}

	public Boolean getBooleanVal() {
		return booleanVal;
	}

	public void setBooleanVal(Boolean booleanVal) {
		this.booleanVal = booleanVal;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public UUID getSomeUuid() {
		return someUuid;
	}

	public void setSomeUuid(UUID someUuid) {
		this.someUuid = someUuid;
	}
}
