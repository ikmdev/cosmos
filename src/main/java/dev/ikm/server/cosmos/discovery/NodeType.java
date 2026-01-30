package dev.ikm.server.cosmos.discovery;

public enum NodeType {
	CONCEPT_CHRONOLOGY("Concept Chronology"),
	CONCEPT_VERSION("Concept Version"),
	SEMANTIC_CHRONOLOGY("Semantic Chronology"),
	SEMANTIC_VERSION("Semantic Version"),
	PATTERN_CHRONOLOGY("Pattern Chronology"),
	PATTERN_VERSION("Pattern Version"),
	STAMP_CHRONOLOGY("STAMP Chronology"),
	STAMP_VERSION("STAMP Version"),
	FIELD("Field");

	private final String displayName;

	NodeType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
