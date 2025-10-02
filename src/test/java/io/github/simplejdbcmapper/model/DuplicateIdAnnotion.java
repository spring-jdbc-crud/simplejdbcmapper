package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "annotation_check")
public class DuplicateIdAnnotion {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@Id
	private Integer id2;

	@Column
	private String something;
}
