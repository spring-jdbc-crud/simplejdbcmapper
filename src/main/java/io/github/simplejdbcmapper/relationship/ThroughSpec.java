package io.github.simplejdbcmapper.relationship;

public interface ThroughSpec {
	PopulateSpec ids(String mainObjIdProperty, String relatedObjIdProperty);
}
