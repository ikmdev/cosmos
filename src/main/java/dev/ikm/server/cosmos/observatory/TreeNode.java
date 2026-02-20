package dev.ikm.server.cosmos.observatory;

import java.util.List;

public record TreeNode(
		int id,
		String name,
		boolean selectable,
		List<TreeNode> children) {
}
