package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.ike.Facade;

import java.util.List;

public record TreeData(
		Facade facade,
		List<TreeData> children,
		boolean expandable,
		boolean selectable) {
}
