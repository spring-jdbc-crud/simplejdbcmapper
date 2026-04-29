package io.github.simplejdbcmapper.relationship;

public interface ToOneSpec {
	PopulateSpec joinOn(String mainObjFkProperty, String relatedObjIdProperty);
}
