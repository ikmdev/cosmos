package dev.ikm.server.cosmos.discovery;

public record ExplorerSearchForm(
		String query,
		Boolean latestVersionOnly,
		Integer maxResults) {
}
