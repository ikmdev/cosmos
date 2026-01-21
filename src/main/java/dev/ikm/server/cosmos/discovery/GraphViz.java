package dev.ikm.server.cosmos.discovery;

import java.util.List;

public record GraphViz(
		List<Node> nodes,
		List<Link> links) {
}
