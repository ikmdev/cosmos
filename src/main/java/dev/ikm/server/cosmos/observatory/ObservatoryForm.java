package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

import java.util.Set;

public record ObservatoryForm(
		String name,
		@StringToFacade Facade selectedStampCoordinate,
		@StringToFacade Facade selectedLanguageCoordinate,
		@StringToFacade Facade selectedNavigationCoordinate,
		@StringToFacade Set<Facade> selectedIncludedModules,
		@StringToFacade Set<Facade> selectedExcludedModules,
		@StringToFacade Set<Facade> selectedIncludedScopes) {
}
