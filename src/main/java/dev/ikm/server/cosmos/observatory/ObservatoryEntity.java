package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.calculator.Language;
import dev.ikm.server.cosmos.calculator.Navigation;
import dev.ikm.server.cosmos.calculator.Stamp;
import dev.ikm.tinkar.common.id.PublicId;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ObservatoryEntity(
		UUID id,
		Instant modified,
		String name,
		Stamp stamp,
		Language language,
		Navigation navigation,
		List<List<UUID>> includedModules,
		List<List<UUID>> excludedModules) implements Serializable {
}
