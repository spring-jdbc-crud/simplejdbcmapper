package io.github.simplejdbcmapper.model;

import java.sql.Types;
import java.time.OffsetDateTime;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "id_sqltype_overridden")
public class IdSqlTypeOverridden {

	@Id
	@Column(sqlType = Types.TIMESTAMP_WITH_TIMEZONE)
	private OffsetDateTime id;

	@Column
	private String comments;

	public OffsetDateTime getId() {
		return id;
	}

	public void setId(OffsetDateTime id) {
		this.id = id;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
