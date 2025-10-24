package dev.ikm.server.cosmos.api.coordinate;

import java.util.List;
import java.util.UUID;

public record CoordinateDTO(
		String name,
		List<UUID> publicId) {
}
