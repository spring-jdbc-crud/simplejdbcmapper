package io.github.simplejdbcmapper.model;

import java.sql.Types;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "type_check")
public class ClobErr {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	// CLOB being mapped to invalid type
	@Column(sqlType = Types.CLOB)
	private char[] clobData;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public char[] getClobData() {
		return clobData;
	}

	public void setClobData(char[] clobData) {
		this.clobData = clobData;
	}

}
