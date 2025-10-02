package io.github.simplejdbcmapper.model;

import java.time.LocalDateTime;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.CreatedBy;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.annotation.UpdatedBy;

@Table(name = "annotation_check")
public class ConflictAnnotation3 {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@Column
	private String something;

	@CreatedBy
	@UpdatedBy
	private LocalDateTime createdBy1;
}
