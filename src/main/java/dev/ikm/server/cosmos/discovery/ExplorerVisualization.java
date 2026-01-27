package dev.ikm.server.cosmos.discovery;

import java.util.List;

public record ExplorerVisualization(
		List<Node> nodes,
		List<Link> links) {
}
