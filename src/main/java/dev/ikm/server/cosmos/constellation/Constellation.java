package dev.ikm.server.cosmos.constellation;

import java.util.Set;
import java.util.UUID;

import dev.ikm.server.cosmos.ike.Facade;

public record Constellation(
		UUID id,
		UUID observatoryId,
		String phase,
		String step,
		String name,
		Set<Facade> scopes,
		String portalPrompt,
		String created,
		long processed,
		String duration,
		boolean isCompleted) {
}
