package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

import java.util.List;

public record ObservatoryForm(
		String name,
		@StringToFacade Facade selectedStampCoordinate,
		@StringToFacade Facade selectedLanguageCoordinate,
		@StringToFacade Facade selectedNavigationCoordinate,
		@StringToFacade List<Facade> selectedIncludedModules,
		@StringToFacade List<Facade> selectedExcludedModules
) {
}
