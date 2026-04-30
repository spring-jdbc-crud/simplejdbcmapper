package io.github.simplejdbcmapper.relationship;

import java.util.List;

public class Relationship<T> implements RelationshipSpec<T> {

	private List<T> mainObjList;

	private Relationship(List<T> mainObjList) {
		this.mainObjList = mainObjList;
	}

	public static <T> RelationshipSpec<T> mainList(List<T> mainObjList) {
		return new Relationship<>(mainObjList);
	}

	public <U> ToOneSpec<T, U> toOneList(List<U> relatedObjList) {
		return ToOne.toOne(mainObjList, relatedObjList);
	}

	public <U> ToManySpec<T, U> toManyList(List<U> relatedObjList) {
		return ToMany.toMany(mainObjList, relatedObjList);
	}

}
