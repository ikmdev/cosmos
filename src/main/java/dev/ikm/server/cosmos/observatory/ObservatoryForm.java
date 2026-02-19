package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

import java.util.List;

public record ObservatoryForm(
		String name,
		List<Facade> stampCoordinates,
		List<Facade> languageCoordinates,
		List<Facade> navigationCoordinates,
		List<Facade> modules,
		@StringToFacade Facade selectedStampCoordinate,
		@StringToFacade Facade selectedLanguageCoordinate,
		@StringToFacade Facade selectedNavigationCoordinate,
		@StringToFacade List<Facade> selectedIncludedModules,
		@StringToFacade List<Facade> selectedExcludedModules,
		TreeData hierarchy,
		@StringToFacade List<Facade> individualScopes,
		@StringToFacade List<Facade> descendantScopes,
		@StringToFacade List<Facade> kindOfScopes) {
}
