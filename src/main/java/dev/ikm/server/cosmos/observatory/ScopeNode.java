package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

public record ScopeNode(
		Facade facade,
		boolean isLeaf) {
}
