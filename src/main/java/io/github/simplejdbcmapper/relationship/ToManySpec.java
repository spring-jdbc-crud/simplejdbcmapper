package io.github.simplejdbcmapper.relationship;

import java.util.List;

public interface ToManySpec<T, U> {

	PopulateSpec joinOn(String mainObjIdProperty, String relatedObjFkProperty);

	ThroughSpec through(List<?> intermediateList, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId);
}
