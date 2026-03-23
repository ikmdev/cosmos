package dev.ikm.server.cosmos.observatory;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import dev.ikm.server.cosmos.calculator.LanguageCoordinate;
import dev.ikm.server.cosmos.calculator.NavigationCoordinate;
import dev.ikm.server.cosmos.calculator.StampCoordinate;
import dev.ikm.server.cosmos.ike.Facade;

public record ObservatoryEntity(
		UUID id,
		Instant modified,
		String name,
		StampCoordinate stampCoordinate,
		LanguageCoordinate languageCoordinate,
		NavigationCoordinate navigationCoordinate,
		Set<Facade> includedModules,
		Set<Facade> excludedModules) implements Serializable {
}
