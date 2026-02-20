package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

public record ScopeItem(
		Facade facade,
		int childrenCount,
		int kindsOfCount) {
}
