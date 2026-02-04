package dev.ikm.server.cosmos.discovery;

public enum NodeType {
	CONCEPT_CHRONOLOGY("Concept Chronology", 10, 1),
	CONCEPT_VERSION("Concept Version", 13, 1),
	SEMANTIC_CHRONOLOGY("Semantic Chronology", 20, 1),
	SEMANTIC_VERSION("Semantic Version", 23, 1),
	PATTERN_CHRONOLOGY("Pattern Chronology", 30, 1),
	PATTERN_VERSION("Pattern Version", 33, 1),
	PATTERN_FIELD("Pattern Field", 36, 1),
	STAMP_CHRONOLOGY("STAMP Chronology", 40, 1),
	STAMP_VERSION("STAMP Version", 43, 1);

	private final String displayName;
	private final int groupId;
	private final int size;

	NodeType(String displayName, int groupId, int size) {
		this.displayName = displayName;
		this.groupId = groupId;
		this.size = size;

	}

	public String displayName() {
		return displayName;
	}

	public int groupId() {
		return groupId;
	}

	public int size() {
		return size;
	}

}
