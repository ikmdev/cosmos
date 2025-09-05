package dev.ikm.server.database.entity;

import java.util.List;

public record SemanticVersionView(
		StampChronologyView stampChronologyView,
		List<Object> values) {
}
