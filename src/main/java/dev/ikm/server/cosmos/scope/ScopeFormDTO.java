package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.CoordinateDTO;

import java.util.List;
import java.util.UUID;

public record ScopeFormDTO(
		String name,
		List<CoordinateDTO> stampCoordinates,
		List<CoordinateDTO> languageCoordinates,
		List<CoordinateDTO> navigationCoordinates,
		UUID selectedStampCoordinateId,
		UUID selectedLanguageCoordinateId,
		UUID selectedNavigationCoordinateId
) {
}
