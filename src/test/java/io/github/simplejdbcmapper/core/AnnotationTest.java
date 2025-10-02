package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.exception.AnnotationException;
import io.github.simplejdbcmapper.model.BlankTableObject;
import io.github.simplejdbcmapper.model.ConflictAnnotation;
import io.github.simplejdbcmapper.model.ConflictAnnotation2;
import io.github.simplejdbcmapper.model.ConflictAnnotation3;
import io.github.simplejdbcmapper.model.DuplicateCreatedByAnnotaition;
import io.github.simplejdbcmapper.model.DuplicateCreatedOnAnnotation;
import io.github.simplejdbcmapper.model.DuplicateIdAnnotion;
import io.github.simplejdbcmapper.model.DuplicateUpdatedByAnnotation;
import io.github.simplejdbcmapper.model.DuplicateUpdatedOnAnnotation;
import io.github.simplejdbcmapper.model.DuplicateVersionAnnotation;
import io.github.simplejdbcmapper.model.InvalidTableObject;
import io.github.simplejdbcmapper.model.NoIdObject;
import io.github.simplejdbcmapper.model.NoMatchingColumn;
import io.github.simplejdbcmapper.model.NoMatchingColumn2;
import io.github.simplejdbcmapper.model.NoTableAnnotationModel;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class AnnotationTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void noTableAnnotation_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(NoTableAnnotationModel.class, 1);
		});
		assertTrue(exception.getMessage().contains("does not have the @Table annotation"));
	}

	@Test
	void invalidTable_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(InvalidTableObject.class, 1);
		});
		assertTrue(exception.getMessage().contains("Unable to locate meta-data for table"));
	}

	@Test
	void blankTable_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(BlankTableObject.class, 1);
		});
		assertTrue(exception.getMessage().contains("@Table annotation has a blank name"));
	}

	@Test
	void noIdObject_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(NoIdObject.class, 1);
		});
		assertTrue(exception.getMessage().contains("@Id annotation not found"));
	}

	@Test
	void noMatchingColumn_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(NoMatchingColumn.class, 1);
		});
		assertTrue(exception.getMessage().contains("column not found in table"));
	}

	@Test
	void noMatchingColumn2_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(NoMatchingColumn2.class, 1);
		});
		assertTrue(exception.getMessage().contains("column not found in table"));
	}

	@Test
	void duplicateIdAnnotation_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(DuplicateIdAnnotion.class, 1);
		});
		assertTrue(exception.getMessage().contains("has multiple @Id annotations"));
	}

	@Test
	void duplicateVersionAnnotation_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(DuplicateVersionAnnotation.class, 1);
		});
		assertTrue(exception.getMessage().contains("has multiple @Version annotations"));
	}

	@Test
	void duplicateCreatedOnAnnotation_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(DuplicateCreatedOnAnnotation.class, 1);
		});
		assertTrue(exception.getMessage().contains("has multiple @CreatedOn annotations"));
	}

	@Test
	void duplicateCreatedByAnnotation_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(DuplicateCreatedByAnnotaition.class, 1);
		});
		assertTrue(exception.getMessage().contains("has multiple @CreatedBy annotations"));
	}

	@Test
	void duplicateUpdatedOnAnnotation_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(DuplicateUpdatedOnAnnotation.class, 1);
		});
		assertTrue(exception.getMessage().contains("has multiple @UpdatedOn annotations"));
	}

	@Test
	void duplicateUpdatedByAnnotation_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(DuplicateUpdatedByAnnotation.class, 1);
		});
		assertTrue(exception.getMessage().contains("has multiple @UpdatedBy annotations"));
	}

	@Test
	void conflictingAnnotations_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(ConflictAnnotation.class, 1);
		});
		assertTrue(exception.getMessage().contains("id has multiple annotations that conflict"));
	}

	@Test
	void conflictingAnnotations2_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(ConflictAnnotation2.class, 1);
		});
		assertTrue(exception.getMessage().contains("has multiple annotations that conflict"));
	}

	@Test
	void conflictingAnnotations3_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(ConflictAnnotation3.class, 1);
		});
		assertTrue(exception.getMessage().contains("has multiple annotations that conflict"));
	}

}
