package dev.ikm.server.cosmos.observatory;

import java.util.Set;

import dev.ikm.server.cosmos.ike.Facade;

public record ObservatoryForm(
		String name,
		@StringToFacade Facade selectedStampCoordinate,
		@StringToFacade Facade selectedLanguageCoordinate,
		@StringToFacade Facade selectedNavigationCoordinate,
		@StringToFacade Set<Facade> selectedIncludedModules,
		@StringToFacade Set<Facade> selectedExcludedModules) {
}
