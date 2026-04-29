package io.github.simplejdbcmapper.relationship;

import java.util.List;

public interface ToOneSpec {
	ToOneSpec joinOn(String mainObjFkProperty, String relatedObjIdProperty);

	ToOneSpec populate(String propertyToPopulateOnMainObj);

	<T, U> void build(List<T> mainObjList, List<U> relatedObjList);
}
