package io.github.simplejdbcmapper.relationship;

import java.util.List;

public interface RelationshipSpec {

	ToOneSpec toOneList(List<?> relatedObjList);

	ToManySpec toManyList(List<?> relatedObjList);

}
