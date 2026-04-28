package io.github.simplejdbcmapper.core;

public class MultiEntity {
	private EntityEntry[] entries;

	public MultiEntity(EntityEntry... entries) {
		if (entries.length == 0) {
			throw new IllegalArgumentException("no entries provided");
		}
		this.entries = entries;
	}

	public EntityEntry[] getEntries() {
		return entries;
	}

}
