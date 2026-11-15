package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "xyz")
public class WhitespaceColumnObject {
	@Id
	private Integer id;

	@Column(name = "abc def")
	private String username;
}