package dev.ikm.server.cosmos.observatory;

import java.util.List;
import java.util.UUID;

public record ModuleConcept(
		List<UUID> id,
		String name) {
}
