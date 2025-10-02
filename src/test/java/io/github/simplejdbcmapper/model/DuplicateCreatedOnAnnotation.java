package io.github.simplejdbcmapper.model;

import java.time.LocalDateTime;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.CreatedOn;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "annotation_check")
public class DuplicateCreatedOnAnnotation {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@CreatedOn
	private LocalDateTime createdOn1;

	@CreatedOn
	private LocalDateTime createdOn2;

	@Column
	private String something;
}
