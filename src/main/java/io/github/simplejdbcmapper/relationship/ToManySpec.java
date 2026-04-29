package io.github.simplejdbcmapper.relationship;

import java.util.List;

public interface ToManySpec {

	ToManySpec joinOn(String mainObjIdProperty, String relatedObjFkProperty);

	ToManySpec populate(String propertyToPopulateOnMainObj);

	ThroughSpec through(List<?> intermediateList, String fkPropertyToMainObjId, String fkPropertyToelatedObjId);

	<T, U> void build(List<T> mainObjList, List<U> relatedObjList);

}
