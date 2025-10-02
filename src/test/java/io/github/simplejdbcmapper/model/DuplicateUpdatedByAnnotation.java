package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.annotation.UpdatedBy;

@Table(name = "annotation_check")
public class DuplicateUpdatedByAnnotation {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@UpdatedBy
	private String updatedBy1;

	@UpdatedBy
	private String updatedBy2;

	@Column
	private String something;
}
