package dev.ikm.server.cosmos.observatory;

import java.util.Set;
import java.util.UUID;

import dev.ikm.server.cosmos.ike.Facade;

public record Observatory(
		UUID id,
		String name,
		@StringToFacade Facade stampCoordinate,
		@StringToFacade Facade languageCoordinate,
		@StringToFacade Facade navigationCoordinate,
		@StringToFacade Set<Facade> includedModules,
		@StringToFacade Set<Facade> excludedModules) {
}
