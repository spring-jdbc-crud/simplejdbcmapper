package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.relationship.Relationship;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class RelationshipTest {
	@Test
	void Relationship_test() {
		assertDoesNotThrow(() -> {
			Relationship.mainList(null);
		});
		assertDoesNotThrow(() -> {
			Relationship.mainList(null).toOneList(null);
		});
		assertDoesNotThrow(() -> {
			Relationship.mainList(null).toManyList(null);
		});
	}
}
