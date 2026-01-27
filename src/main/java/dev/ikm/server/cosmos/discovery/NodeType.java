package dev.ikm.server.cosmos.discovery;

public enum NodeType {
	CONCEPT("Concept"),
	SEMANTIC("Semantic"),
	PATTERN("Pattern"),
	STAMP("STAMP"),
	FIELD("Field");

	private final String displayName;

	NodeType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
