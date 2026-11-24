package io.github.simplejdbcmapper.core;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.library.dependencies.SliceAssignment;
import com.tngtech.archunit.library.dependencies.SliceIdentifier;
import org.junit.jupiter.api.Test;

class ArchitectureCycleTest {
  @Test
  void packagesShouldBeFreeOfCycles() {
    JavaClasses importedClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("io.github.simplejdbcmapper");

    slices()
        .matching("io.github.simplejdbcmapper.(*)..")
        .should()
        .beFreeOfCycles()
        .check(importedClasses);
  }

  @Test
  void classesShouldBeFreeOfCycles() {
    JavaClasses importedClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .withImportOption(
                new ImportOption() {
                  @Override
                  public boolean includes(Location location) {
                    boolean val = true;
                    // ignore relationship inner classes
                    if (location.contains("Relationship$RelationshipBuilder")
                        || location.contains("RelationshipFluent$")) {
                      val = false;
                    }
                    return val;
                  }
                })
            .importPackages("io.github.simplejdbcmapper");

    slices()
        .assignedFrom(
            new SliceAssignment() {
              @Override
              public SliceIdentifier getIdentifierOf(JavaClass javaClass) {
                return SliceIdentifier.of(javaClass.getName());
              }

              @Override
              public String getDescription() {
                return "each individual class";
              }
            })
        .should()
        .beFreeOfCycles()
        .check(importedClasses);
  }
}
