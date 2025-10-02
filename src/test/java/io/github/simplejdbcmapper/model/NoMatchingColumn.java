package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "product")
public class NoMatchingColumn {
	@Id
	private Integer idSomething;

	@Column
	private Double cost;
}
