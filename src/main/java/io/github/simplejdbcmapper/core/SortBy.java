package io.github.simplejdbcmapper.core;

import org.springframework.util.Assert;

/**
 * The is used to generate the "ORDER BY" clause for the queries.
 * 
 * @author Antony Joseph
 */
public class SortBy {
	private final String propertyName;
	private final String direction;

	/**
	 * The constructor
	 * 
	 * @param propertyName the property name to sort by. (NOT the column name)
	 */
	public SortBy(String propertyName) {
		this(propertyName, null);
	}

	/**
	 * The constructor
	 * 
	 * @param propertyName the property name to sort by (NOT the column name)
	 * @param direction    the direction of the sort. Has to be either "ASC" or
	 *                     "DESC"
	 */
	public SortBy(String propertyName, String direction) {
		Assert.notNull(propertyName, "propertyName must not be null");
		this.propertyName = propertyName;
		if (direction != null) {
			if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) {
				throw new IllegalArgumentException("direction should be ASC or DESC");
			}
			this.direction = direction.toUpperCase();
		} else {
			this.direction = "";
		}
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getDirection() {
		return direction;
	}
}
