package io.github.simplejdbcmapper.core;

import org.postgresql.util.PGobject;

import io.github.simplejdbcmapper.model.Product;
import tools.jackson.databind.ObjectMapper;

/**
 * Custom converter to store an object into a 'jsonb' column in a postgres
 * database and retrieve the data back into the entity attribute.
 * 
 * <pre>
 * See the model io.github.simplejdbcmapper.model.JsonBData.java for how to
 *      configure this converter to an entity attribute.
 *   
 * The related postgres table definition:
 * 
 * CREATE TABLE jsonb_data (
 *	id serial NOT NULL,
 *	product_json jsonb,
 *	CONSTRAINT jsonb_data_pk PRIMARY KEY (id));
 * 
 * </pre>
 */
public class ProductToJsonPostgresConverter implements AttributeConverter<Product, PGobject> {
	private final ObjectMapper objectMapper = new ObjectMapper();

	// This is for postgres database table where the database column is of type
	// 'jsonb'
	@Override
	public PGobject convertToDatabaseColumn(Product source) {
		try {
			PGobject pgObject = new PGobject();
			String json = objectMapper.writeValueAsString(source);
			pgObject.setType("jsonb"); // Need this for the postgres driver
			pgObject.setValue(json);
			return pgObject;
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to convert Product to Json string", e);
		}
	}

	@Override
	public Product convertToEntityAttribute(PGobject dbData) {
		if (dbData == null) {
			return null;
		}
		try {
			String json = dbData.getValue();

			// Note: For example in case of a generic like List<User>
			// due to type erasure at runtime, we would need to use TypeReference
			// In that case, code would look something like:
			// return objectMapper.readValue(dbData, new TypeReference<List<User>>() {});

			// In this example since there are no generics we just use the Product.class as
			// argument
			return objectMapper.readValue(json, Product.class);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error converting JSON string to Product.", e);
		}
	}

}
