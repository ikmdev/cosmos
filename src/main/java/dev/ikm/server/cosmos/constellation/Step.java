package dev.ikm.server.cosmos.constellation;

public enum Step {

	PROCESS_CONCEPTS("Processing Concept Knowledge"),
	PROCESS_HIERARCHY("Processing Hierarchy Knowledge"),
	PROCESS_LOGICAL_DEFINITIONS("Processing Logical Definition Knowledge"),
	PROCESS_SEMANTICS("Processing Semantic Knowledge");

	private final String display;

	private Step(String display) {
		this.display = display;
	}

	public String getDisplay() {
		return display;
	}

}
