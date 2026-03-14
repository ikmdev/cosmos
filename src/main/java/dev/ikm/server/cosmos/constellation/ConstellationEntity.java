package dev.ikm.server.cosmos.constellation;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import dev.ikm.server.cosmos.ike.Facade;

public record ConstellationEntity(
		UUID id,
		UUID observatoryId,
		Phase phase,
		String name,
		Set<Facade> scopes,
		String portalPrompt,
		long processed,
		Instant created,
		Instant completed) implements Serializable {

	public ConstellationEntity with(Phase phase) {
		return new ConstellationEntity(id, observatoryId, phase, name, scopes, portalPrompt, processed, created, completed);
	}

	public ConstellationEntity with(Instant completed) {
		return new ConstellationEntity(id, observatoryId, phase, name, scopes, portalPrompt, processed, created, completed);
	}

	public ConstellationEntity with(long processed) {
		return new ConstellationEntity(id, observatoryId, phase, name, scopes, portalPrompt, processed, created, completed);
	}

	public boolean isCompleted() {
		return completed != null;
	}

	public Duration getDuration() {
		Instant endTime = isCompleted() ? completed : Instant.now();
		return Duration.between(created, endTime);
	}
}
