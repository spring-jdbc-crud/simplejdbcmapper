package io.github.simplejdbcmapper.core;

import org.postgresql.util.PGobject;

import io.github.simplejdbcmapper.model.Product;
import tools.jackson.databind.ObjectMapper;

public class ProductToJsonConverter implements AttributeConverter<Product, PGobject> {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public PGobject convertToDatabaseColumn(Product source) {
		try {
			PGobject pgObject = new PGobject();
			String json = objectMapper.writeValueAsString(source);
			pgObject.setType("jsonb");
			pgObject.setValue(json);
			return pgObject;
			// return json;
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
			return objectMapper.readValue(json, Product.class);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error converting JSON string to Product.", e);
		}
	}

}
