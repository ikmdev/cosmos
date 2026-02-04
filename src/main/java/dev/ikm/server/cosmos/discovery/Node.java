package dev.ikm.server.cosmos.discovery;

import java.util.List;

public record Node(
		String id,
		String type,
		String label,
		int group,
		int size,
		List<String> values) {
}
