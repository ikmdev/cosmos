package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

import java.util.List;
import java.util.UUID;

public record Observatory(
		UUID id,
		String name,
		@StringToFacade Facade stampCoordinate,
		@StringToFacade Facade languageCoordinate,
		@StringToFacade Facade navigationCoordinate,
		@StringToFacade List<Facade> includedModules,
		@StringToFacade List<Facade> excludedModules,
		@StringToFacade List<Facade> includedScopes) {
}
