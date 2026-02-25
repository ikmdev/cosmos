package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

public record ScopeSearchResult(
		Facade facade,
		int childrenCount,
		int descendantCount) {
}
