package dev.ikm.server.cosmos.constellation;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record ConstellationEntity(
		UUID id,
		String name,
		long concepts,
		long semantics,
		long patterns,
		long total,
		long progress,
		Instant creation,
		Instant start,
		Instant end,
		boolean isCompleted) implements Serializable {
}
