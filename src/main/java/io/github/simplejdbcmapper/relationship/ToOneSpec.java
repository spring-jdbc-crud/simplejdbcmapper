package io.github.simplejdbcmapper.relationship;

public interface ToOneSpec<T, U> {
	PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty);
}
