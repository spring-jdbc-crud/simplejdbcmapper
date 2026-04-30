package io.github.simplejdbcmapper.relationship;

import java.util.List;

public interface RelationshipSpec<T> {

	<U> ToOneSpec<T, U> toOneList(List<U> relatedObjList);

	<U> ToManySpec<T, U> toManyList(List<U> relatedObjList);

}
