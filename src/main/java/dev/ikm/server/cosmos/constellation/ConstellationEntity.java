package dev.ikm.server.cosmos.constellation;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record ConstellationEntity(
		UUID id,
		String name,
		long concepts,
		long semantics,
		long patterns,
		Instant created,
		Instant completed) implements Serializable {

	public boolean isCompleted() {
		return completed != null;
	}

	public Duration getDuration() {
		Instant endTime = isCompleted() ? completed : Instant.now();
		return Duration.between(created, endTime);
	}

	public long total() {
		return concepts + semantics + patterns;
	}

}
