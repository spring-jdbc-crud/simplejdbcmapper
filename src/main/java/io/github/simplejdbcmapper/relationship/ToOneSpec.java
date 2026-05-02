package io.github.simplejdbcmapper.relationship;

/**
 * Specification for the ToOne relationship
 * 
 * @param <T> The main object type
 * @param <U> The related object type
 * 
 * @author Antony Joseph
 */
public interface ToOneSpec<T, U> {
	/**
	 * The properties on the two lists whose values should be matched to populate
	 * the toOne relationship
	 * 
	 * @param mainObjJoinProperty    The main object property for matching
	 * @param relatedObjJoinProperty The related object property for matching
	 * @return The populateSpec
	 * 
	 * @author Antony Joseph
	 */
	PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty);
}
