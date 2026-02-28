package dev.ikm.server.cosmos.constellation;

import java.util.UUID;

public record Constellation(
		UUID id,
		UUID observatoryId,
		String phase,
		String name,
		String created,
		long processed,
		String duration,
		boolean isCompleted) {
}
