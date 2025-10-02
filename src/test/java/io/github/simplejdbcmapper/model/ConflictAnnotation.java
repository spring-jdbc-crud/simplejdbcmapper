package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.annotation.Version;

@Table(name = "annotation_check")
public class ConflictAnnotation {
	@Id(type = IdType.AUTO_GENERATED)
	@Version
	private Integer id;

	@Column
	private String something;
}
