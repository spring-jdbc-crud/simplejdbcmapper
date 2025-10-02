package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.annotation.Version;

@Table(name = "annotation_check")
public class DuplicateVersionAnnotation {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer id;

	@Version
	private Integer version1;

	@Version
	private Integer version2;

	@Column
	private String something;
}
