package dev.ikm.server.cosmos.discovery;

public record Node(
		String id,
		NodeType type,
		String label,
		int group,
		int size) {
}
