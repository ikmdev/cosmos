package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.Coordinate;

import java.util.UUID;

public record ScopeDTO(
		UUID id,
		String name,
		Coordinate stampCoordinate,
		Coordinate languageCoordinate,
		Coordinate navigationCoordinate) {
}
