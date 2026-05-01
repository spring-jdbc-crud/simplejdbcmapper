package io.github.simplejdbcmapper.relationship;

import java.util.List;

/**
 * A relationship specification for populating relationships
 */
public interface RelationshipSpec<T> {

	/**
	 * A toOne relationship
	 * 
	 * @param <T>            the type of the main object
	 * @param <U>            the type of the related object
	 * 
	 * @param relatedObjList the list of related objects
	 */
	<U> ToOneSpec<T, U> toOneList(List<U> relatedObjList);

	/**
	 * A toMany relationship
	 * 
	 * @param <T>            the type of the main object
	 * @param <U>            the type of the related object
	 * 
	 * @param relatedObjList the list of related objects
	 */
	<U> ToManySpec<T, U> toManyList(List<U> relatedObjList);

}
