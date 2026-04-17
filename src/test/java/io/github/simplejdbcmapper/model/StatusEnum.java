package io.github.simplejdbcmapper.model;

public enum StatusEnum {
	OPEN, CLOSED;

	@Override
	public String toString() {
		return this.name() + "x";
	}
}
