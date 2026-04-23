package io.github.simplejdbcmapper.model;

import java.sql.Clob;
import java.sql.Types;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "type_check")
public class ClobType {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@Column(sqlType = Types.BLOB)
	private Clob clobData;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Clob getClobData() {
		return clobData;
	}

	public void setClob(Clob clobData) {
		this.clobData = clobData;

	}
}
