package dev.ikm.server.cosmos.constellation;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record ConstellationEntity(
		UUID id,
		Phase phase,
		String name,
		long concepts,
		long semantics,
		long patterns,
		Instant created,
		Instant completed) implements Serializable {

	public ConstellationEntity with(Phase phase) {
		return new ConstellationEntity(id, phase, name, concepts, semantics, patterns, created, completed);
	}

	public ConstellationEntity with(Instant completed) {
		return new ConstellationEntity(id, phase, name, concepts, semantics, patterns, created, completed);
	}

	public ConstellationEntity with(long concepts, long semantics, long patterns) {
		return new ConstellationEntity(id, phase, name, concepts, semantics, patterns, created, completed);
	}

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
