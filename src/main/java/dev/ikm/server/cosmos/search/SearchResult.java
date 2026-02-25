package dev.ikm.server.cosmos.search;

import java.util.List;
import java.util.UUID;

public record SearchResult(
		List<UUID> id,
		String text) {
}
