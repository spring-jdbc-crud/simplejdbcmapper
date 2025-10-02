package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "    ")
public class BlankTableObject {
	@Id
	private Integer id;
}
