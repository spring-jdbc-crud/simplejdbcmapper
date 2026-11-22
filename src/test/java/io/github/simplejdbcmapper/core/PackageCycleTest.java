package io.github.simplejdbcmapper.core;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class PackageCycleTest {
	@Test
	void packagesShouldBeFreeOfCycles() {
		JavaClasses importedClasses = new ClassFileImporter()
				.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
				.importPackages("io.github.simplejdbcmapper");

		slices().matching("io.github.simplejdbcmapper.(*)..").should().beFreeOfCycles().check(importedClasses);
	}
}
