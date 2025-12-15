package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.CoordinateDTO;
import dev.ikm.server.cosmos.api.coordinate.Language;
import dev.ikm.server.cosmos.api.coordinate.Navigation;
import dev.ikm.server.cosmos.api.coordinate.Stamp;

import java.util.UUID;

public record ScopeDTO(
		UUID id,
		String name,
		CoordinateDTO stampCoordinate,
		CoordinateDTO languageCoordinate,
		CoordinateDTO navigationCoordinate) {
}
