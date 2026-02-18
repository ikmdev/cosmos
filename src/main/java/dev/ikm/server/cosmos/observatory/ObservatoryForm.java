package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

import java.util.List;

public record ObservatoryForm(
		String name,
		List<Facade> stampCoordinates,
		List<Facade> languageCoordinates,
		List<Facade> navigationCoordinates,
		List<Facade> modules,
		Facade selectedStampCoordinate,
		Facade selectedLanguageCoordinate,
		Facade selectedNavigationCoordinate,
		List<Facade> selectedIncludedModules,
		List<Facade> selectedExcludedModules) {
}
