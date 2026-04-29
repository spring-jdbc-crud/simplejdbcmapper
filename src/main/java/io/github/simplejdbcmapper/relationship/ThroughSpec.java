package io.github.simplejdbcmapper.relationship;

import java.util.List;

public interface ThroughSpec {
	ThroughSpec ids(String mainObjIdProperty, String relatedObjIdProperty);

	ToManySpec populate(String propertyToPopulateOnMainObj);

	<T, U> void build(List<T> mainObjList, List<U> relatedObjList);

}
