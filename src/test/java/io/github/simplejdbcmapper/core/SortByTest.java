package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class SortByTest {

	@Test
	void SortBy_success_test() {
		Assertions.assertDoesNotThrow(() -> new SortBy("someProperty"));
		Assertions.assertDoesNotThrow(() -> new SortBy("someProperty", "ASC"));
		Assertions.assertDoesNotThrow(() -> new SortBy("someProperty", "DESC"));
	}

	@Test
	void SortBy_failure_test() {
		Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new SortBy(null);
		});
		assertTrue(exception.getMessage().contains("propertyName must not be null"));

		exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new SortBy("someProperty", "xyz");
		});
		assertTrue(exception.getMessage().contains("direction should be ASC or DESC"));

	}
}
