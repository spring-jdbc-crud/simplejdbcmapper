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
	private String[] clobData;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String[] getClobData() {
		return clobData;
	}

	public void setClobData(String[] clobData) {
		this.clobData = clobData;
	}

}
