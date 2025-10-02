package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "product")
public class NoMatchingColumn2 {
	@Id
	private Integer productId;

	@Column(name = "abc")
	private Double cost;
}
