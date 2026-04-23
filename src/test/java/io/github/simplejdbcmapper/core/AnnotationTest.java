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
import io.github.simplejdbcmapper.model.BlobType;
import io.github.simplejdbcmapper.model.ClobType;
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
import io.github.simplejdbcmapper.model.ModelWithVersionNotInteger;
import io.github.simplejdbcmapper.model.NoIdObject;
import io.github.simplejdbcmapper.model.NoMatchingColumn;
import io.github.simplejdbcmapper.model.NoMatchingColumn2;
import io.github.simplejdbcmapper.model.NoTableAnnotationModel;
import io.github.simplejdbcmapper.model.PersonWithColumnPrimitive;
import io.github.simplejdbcmapper.model.PersonWithOtherPrimitive;
import io.github.simplejdbcmapper.model.PersonWithPrimitiveId;

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
		Assertions.assertThrows(Exception.class, () -> {
			sjm.findById(InvalidTableObject.class, 1);
		});
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
		Assertions.assertThrows(Exception.class, () -> {
			sjm.findById(NoMatchingColumn.class, 1);
		});
	}

	@Test
	void noMatchingColumn2_Test() {
		Assertions.assertThrows(Exception.class, () -> {
			sjm.findById(NoMatchingColumn2.class, 1);
		});
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

	@Test
	void annotationVersionTypeNotInteger_Test() {
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.findById(ModelWithVersionNotInteger.class, 1);
		});
		assertTrue(exception.getMessage().contains("to be Integer"));
	}

	@Test
	void annotation_withIdPrimitive_Test() {
		PersonWithPrimitiveId person = new PersonWithPrimitiveId();
		person.setLastName("john");
		person.setFirstName("doe");

		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.insert(person);
		});

		assertTrue(exception.getMessage().contains("is a primitive. Mapper does not support primitive types."));
	}

	@Test
	void annotation_withColumnPrimitive_Test() {
		PersonWithColumnPrimitive person = new PersonWithColumnPrimitive();
		person.setLastName("john");
		person.setFirstName("doe");
		person.setBoolProperty(false);

		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.insert(person);
		});

		assertTrue(exception.getMessage().contains("is a primitive. Mapper does not support primitive types."));
	}

	@Test
	void annotation_withOtherPrimitive_Test() {
		PersonWithOtherPrimitive person = new PersonWithOtherPrimitive();
		person.setLastName("john");
		person.setFirstName("doe");
		person.setCreatedBy(1);

		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.insert(person);
		});

		assertTrue(exception.getMessage().contains("is a primitive. Mapper does not support primitive types."));
	}

	@Test
	void insert_BlobTypeTest() {
		BlobType obj = new BlobType();
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.insert(obj);
		});
		assertTrue(exception.getMessage().contains("is of type java.sql.Blob and is not supported"));
	}

	@Test
	void insert_ClobTypeTest() {
		ClobType obj = new ClobType();
		Exception exception = Assertions.assertThrows(AnnotationException.class, () -> {
			sjm.insert(obj);
		});
		assertTrue(exception.getMessage().contains("is of type java.sql.Clob and is not supported"));
	}

}
