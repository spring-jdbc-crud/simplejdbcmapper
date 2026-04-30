package io.github.simplejdbcmapper.relationship;

import java.util.List;

public class Relationship implements RelationshipSpec {

	private List<?> mainObjList;

	private Relationship(List<?> mainObjList) {
		this.mainObjList = mainObjList;
	}

	public static RelationshipSpec mainList(List<?> mainObjList) {
		return new Relationship(mainObjList);
	}

	public ToOneSpec toOneList(List<?> relatedObjList) {
		return ToOne.toOne(mainObjList, relatedObjList);
	}

	public ToManySpec toManyList(List<?> relatedObjList) {
		return ToMany.toMany(mainObjList, relatedObjList);
	}

}
