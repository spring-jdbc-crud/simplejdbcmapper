package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "no_id_object")
public class NoIdObject {
	@Column
	private String something;
}
