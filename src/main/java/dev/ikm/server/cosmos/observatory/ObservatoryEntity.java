package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.calculator.LanguageCoordinate;
import dev.ikm.server.cosmos.calculator.NavigationCoordinate;
import dev.ikm.server.cosmos.calculator.StampCoordinate;
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.tinkar.common.id.PublicId;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ObservatoryEntity(
		UUID id,
		Instant modified,
		String name,
		StampCoordinate stampCoordinate,
		LanguageCoordinate languageCoordinate,
		NavigationCoordinate navigationCoordinate,
		List<Facade> includedModules,
		List<Facade> excludedModules,
		List<Facade> includeScopes) implements Serializable {
}
