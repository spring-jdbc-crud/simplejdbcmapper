package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "xyz abc")
public class WhitespaceTableObject {
	@Id
	private Integer id;
}
