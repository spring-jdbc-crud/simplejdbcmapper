package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "xyz")
public class BlankColumnObject {
	@Id
	private Integer id;

	@Column(name = "  ")
	private String username;
}
