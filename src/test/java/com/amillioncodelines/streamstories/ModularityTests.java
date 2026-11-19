package com.amillioncodelines.streamstories;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

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
