package io.github.simplejdbcmapper.model;

import java.time.LocalDateTime;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.annotation.UpdatedOn;

@Table(name = "annotation_check")
public class DuplicateUpdatedOnAnnotation {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@UpdatedOn
	private LocalDateTime updatedOn1;

	@UpdatedOn
	private LocalDateTime updatedOn2;

	@Column
	private String something;
}
