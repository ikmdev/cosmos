package dev.ikm.server.cosmos.discovery;

import java.util.List;

public record ExplorerData(
		List<Node> nodes,
		List<Link> links) {
}
