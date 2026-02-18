package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

import java.util.List;
import java.util.UUID;

public record Observatory(
		UUID id,
		String name,
		Facade stamp,
		Facade language,
		Facade navigation,
		List<Facade> includedModules,
		List<Facade> excludedModules) {
}
