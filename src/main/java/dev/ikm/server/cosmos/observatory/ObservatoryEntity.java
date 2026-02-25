package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.calculator.LanguageCoordinate;
import dev.ikm.server.cosmos.calculator.NavigationCoordinate;
import dev.ikm.server.cosmos.calculator.StampCoordinate;
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
		List<List<UUID>> includedModules,
		List<List<UUID>> excludedModules) implements Serializable {
}
