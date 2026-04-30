package io.github.simplejdbcmapper.relationship;

public interface ToOneSpec {
	PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty);
}
