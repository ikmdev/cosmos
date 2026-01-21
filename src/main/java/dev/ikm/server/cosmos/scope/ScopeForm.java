package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.Coordinate;

import java.util.List;
import java.util.UUID;

public record ScopeForm(
		String name,
		List<Coordinate> stampCoordinates,
		List<Coordinate> languageCoordinates,
		List<Coordinate> navigationCoordinates,
		List<UUID> selectedStampCoordinateId,
		List<UUID> selectedLanguageCoordinateId,
		List<UUID> selectedNavigationCoordinateId
) {
}
