package io.github.simplejdbcmapper.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "type_check")
public class TypeCheckSqlServer {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@Column
	private LocalDate localDateData;

	@Column
	private java.util.Date javaUtilDateData;

	@Column
	private LocalDateTime localDateTimeData;

	@Column
	private java.util.Date javaUtilDateDtData; // SqlServer

	@Column
	private BigDecimal bigDecimalData;

	@Column(name = "string_enum")
	private StatusEnum status;
	
	@Column
	private byte[] image;
	
	@Column
	private char[] clobData;

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

	public java.util.Date getJavaUtilDateDtData() {
		return javaUtilDateDtData;
	}

	public void setJavaUtilDateDtData(java.util.Date javaUtilDateDtData) {
		this.javaUtilDateDtData = javaUtilDateDtData;
	}

	public BigDecimal getBigDecimalData() {
		return bigDecimalData;
	}

	public void setBigDecimalData(BigDecimal bigDecimalData) {
		this.bigDecimalData = bigDecimalData;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
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
