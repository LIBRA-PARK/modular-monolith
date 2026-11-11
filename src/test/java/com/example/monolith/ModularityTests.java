package com.example.monolith;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

	@Test
	void verifiesApplicationModuleBoundariesAndWritesDocumentation() {
		var modules = ApplicationModules.of(MonolithApplication.class).verify();

		new Documenter(modules)
				.writeModulesAsPlantUml()
				.writeIndividualModulesAsPlantUml()
				.writeModuleCanvases()
				.writeAggregatingDocument();
	}

}
