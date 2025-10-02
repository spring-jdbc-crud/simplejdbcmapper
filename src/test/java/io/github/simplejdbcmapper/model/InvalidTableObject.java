package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "invalid_table")
public class InvalidTableObject {
	@Id
	private Integer id;
}
