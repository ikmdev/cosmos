package dev.ikm.server.cosmos.constellation;

import java.util.Set;
import java.util.UUID;

import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.observatory.StringToFacade;

public record Constellation(
		UUID id,
		UUID observatoryId,
		String phase,
		String name,
		@StringToFacade Set<Facade> scopes,
		String portalPrompt,
		String created,
		long processed,
		String duration,
		boolean isCompleted) {
}
