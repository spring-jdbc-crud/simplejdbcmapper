package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.model.NoDefaultConstructor;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class EntityRowMapperTest {

	@Test
	void no_defaultConstructor_Test() {

		EntityRowMapper<NoDefaultConstructor> rowMapper = new EntityRowMapper<>(NoDefaultConstructor.class, null, null);

		Exception exception = Assertions.assertThrows(MapperException.class, () -> {
			rowMapper.mapRow(null, 0);
		});
		assertTrue(exception.getMessage().contains("Could not instantiate class"));

	}
}