package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.scope.Scope;

import java.time.Instant;
import java.util.UUID;

public record ConstellationForm(
		UUID id,
		String name,
		Scope scope) {
}
