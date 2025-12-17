package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.Language;
import dev.ikm.server.cosmos.api.coordinate.Navigation;
import dev.ikm.server.cosmos.api.coordinate.Stamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record ScopeEntity(
		UUID id,
		Instant modified,
		String name,
		Stamp stamp,
		Language language,
		Navigation navigation) implements Serializable {
}
