package com.bohdeveloper.rexiacloud.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Convierte las reglas de arquitectura hexagonal en algo que falla el build
 * si se rompe, en vez de depender de la disciplina en cada PR:
 *  - domain no puede depender de infrastructure ni de Spring.
 *  - application no puede depender de infrastructure (solo de sus puertos).
 */
class HexagonalArchitectureTest {

    private static final JavaClasses CLASES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.bohdeveloper.rexiacloud");

    @Test
    void elDominioNoDependeDeInfraestructura() {
        ArchRule regla = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
        regla.check(CLASES);
    }

    @Test
    void elDominioNoDependeDeSpring() {
        ArchRule regla = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..");
        regla.check(CLASES);
    }

    @Test
    void laAplicacionNoDependeDeInfraestructura() {
        ArchRule regla = noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
        regla.check(CLASES);
    }
}
