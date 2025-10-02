package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.CreatedBy;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "annotation_check")
public class DuplicateCreatedByAnnotaition {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@CreatedBy
	private String createdBy1;

	@CreatedBy
	private String createdBy2;

	@Column
	private String something;
}
