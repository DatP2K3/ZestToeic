package com.zest.toeic;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.zest.toeic", importOptions = { ImportOption.DoNotIncludeTests.class })
class ArchitectureTest {

    @ArchTest
    static final ArchRule shared_should_not_depend_on_domain =
            noClasses().that().resideInAPackage("..shared..")
                    .and().resideOutsideOfPackage("..shared.ai..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..practice..", "..auth..", "..battle..", "..community..", "..gamification..", "..notification..", "..productivity.."
                    );

    @ArchTest
    static final ArchRule controllers_should_not_inject_repositories =
            noClasses().that().areAnnotatedWith(RestController.class)
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule services_should_be_named_Service =
            classes().that().areAnnotatedWith(Service.class)
                    .should().haveSimpleNameEndingWith("Service")
                    .orShould().haveSimpleNameEndingWith("Scraper")
                    .orShould().haveSimpleNameEndingWith("Generator")
                    .orShould().haveSimpleNameEndingWith("Router");
}
