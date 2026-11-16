package com.amillioncodelines.streamstories;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/** Verifies that application modules respect their boundaries (no cycles, no access to internals). */
class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(StreamstoriesApplication.class);

    @Test
    void shouldRespectModuleBoundaries() {
        modules.verify();
    }

    @Test
    void shouldWriteModuleDocumentation() {
        new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml();
    }
}
