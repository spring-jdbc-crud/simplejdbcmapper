package io.github.simplejdbcmapper.relationship;

/**
 * Specification for the ToOne relationship
 * 
 * @author Antony Joseph
 */
public interface ToOneSpec {
	/**
	 * The properties on the two lists whose values should be matched to populate
	 * the toOne relationship. <b>The java type of both the properties have to be
	 * exactly the same.</b>
	 * 
	 * For example one should not be Long and the other an Integer.
	 * 
	 * 
	 * @param mainObjJoinProperty    The main object property for matching
	 * @param relatedObjJoinProperty The related object property for matching
	 * @return The populateSpec
	 * 
	 * @author Antony Joseph
	 */
	PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty);
}
