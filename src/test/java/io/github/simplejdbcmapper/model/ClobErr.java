package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "type_check")
public class ClobErr {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	// CLOB being mapped to invalid type byte[]
	@Column
	private byte[] clobData;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public byte[] getClobData() {
		return clobData;
	}

	public void setClobData(byte[] clobData) {
		this.clobData = clobData;
	}

}
