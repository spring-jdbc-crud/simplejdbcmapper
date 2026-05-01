package io.github.simplejdbcmapper.relationship;

import java.util.List;

/**
 * The starting point for any relationship assignment. It works with the lists
 * provided and depending on the arguments to the fluent api methods, populates
 * the target property.
 * 
 * @param <T> the type of the main list
 */
public class Relationship<T> implements RelationshipSpec<T> {

	private List<T> mainObjList;

	private Relationship(List<T> mainObjList) {
		this.mainObjList = mainObjList;
	}

	/**
	 * Start of creating a relationship
	 * 
	 * @param <T>
	 * @param mainObjList the main object list
	 * @return A relationship
	 */
	public static <T> RelationshipSpec<T> mainList(List<T> mainObjList) {
		return new Relationship<>(mainObjList);
	}

	/**
	 * A toOne relationship
	 * 
	 * @param <T>            the type of the main object
	 * @param <U>            the type of the related object
	 * 
	 * @param relatedObjList the list of related objects
	 * 
	 */
	public <U> ToOneSpec<T, U> toOneList(List<U> relatedObjList) {
		return ToOne.toOne(mainObjList, relatedObjList);
	}

	/**
	 * A toMany relationship
	 * 
	 * @param <T>            the type of the main object
	 * @param <U>            the type of the related object
	 * 
	 * @param relatedObjList the list of related objects
	 */
	public <U> ToManySpec<T, U> toManyList(List<U> relatedObjList) {
		return ToMany.toMany(mainObjList, relatedObjList);
	}

}
