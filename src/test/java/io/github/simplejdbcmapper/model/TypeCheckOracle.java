package io.github.simplejdbcmapper.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "type_check")
public class TypeCheckOracle {
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
	private OffsetDateTime offsetDateTimeData;

	@Column(name = "string_enum")
	private StatusEnum status;

	@Column
	private byte[] image;

	@Column
	private char[] clobData;

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

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

	public OffsetDateTime getOffsetDateTimeData() {
		return offsetDateTimeData;
	}

	public void setOffsetDateTimeData(OffsetDateTime offsetDateTimeData) {
		this.offsetDateTimeData = offsetDateTimeData;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public char[] getClobData() {
		return clobData;
	}

	public void setClobData(char[] clobData) {
		this.clobData = clobData;
	}

}
