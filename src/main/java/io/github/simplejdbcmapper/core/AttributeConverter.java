package io.github.simplejdbcmapper.core;

/**
 * A class that implements this interface can be used to convert entity
 * attribute into database column representation for inserts/updates and vice
 * versa for queries.
 * 
 * You would configure the converter with an Entity attribute using &#64;Column
 * annotation like below:
 * 
 * <pre>
 * &#64;Column(converter = "yourImplementationOfThisInterace.class")
 * </pre>
 * 
 * @param <T> entity attribute
 * @param <U> database column data
 */
public interface AttributeConverter<T, U> {
	/**
	 * Converts the value stored in the entity attribute into the data
	 * representation to be stored in the database.
	 * 
	 * @param attribute the entity attribute
	 * @return the converted data to be stored in the database column
	 */
	U convertToDatabaseColumn(T attribute);

	/**
	 * Converts the data stored in the database column into the value to be stored
	 * in the entity attribute. It is the responsibility of the converter writer to
	 * specify the correct dbData type for the corresponding column for use by the
	 * JDBC driver
	 * 
	 * @param dbData the database representation of the data
	 * @return the converted value to be stored in the entity attribute
	 */
	T convertToEntityAttribute(U dbData);
}
