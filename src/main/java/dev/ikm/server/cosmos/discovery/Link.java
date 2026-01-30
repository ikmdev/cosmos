package dev.ikm.server.cosmos.discovery;

public record Link(
		String type,
		int source,
		int target) {
}
