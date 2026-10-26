package io.github.simplejdbcmapper.core;
/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * A class that implements this interface can be used to convert entity
 * attribute into database column representation and back.
 * 
 * You would configure the converter with an Entity attribute using &#64;Column
 * annotation like below:
 * 
 * <pre>
 * &#64;Column(converter = yourImplementationOfThisInterace.class)
 * </pre>
 * 
 * For more details see the <a href=
 * "https://github.com/spring-jdbc-crud/simplejdbcmapper#custom-converters">documentation</a>
 * 
 * @param <T> entity attribute
 * @param <U> database column data
 * 
 * @author Antony Joseph
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
