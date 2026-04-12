package io.github.simplejdbcmapper.model;

import java.sql.Blob;
import java.sql.Types;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "type_check")
public class BlobErr {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@Column(sqlType = Types.BLOB)
	private Blob image;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Blob getImage() {
		return image;
	}

	public void setImage(Blob image) {
		this.image = image;
	}

}
