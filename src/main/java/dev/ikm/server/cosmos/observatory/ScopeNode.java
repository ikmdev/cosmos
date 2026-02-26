package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

public record ScopeNode(
		@StringToFacade Facade facade,
		boolean isLeaf) {
}
