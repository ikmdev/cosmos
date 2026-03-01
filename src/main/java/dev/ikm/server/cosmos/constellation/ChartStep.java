package dev.ikm.server.cosmos.constellation;

public enum ChartStep {

	WRITE_CONCEPTS("Writing Concept Knowledge"),
	WRITE_HIERARCHY("Writing Hierarchy Knowledge"),
	WRITE_LOGICAL_DEFINITIONS("Writing Logical Definition Knowledge"),
	WRITE_SEMANTICS("Writing Semantic Knowledge");

	private final String display;

	private ChartStep(String display) {
		this.display = display;
	}

	public String getDisplay() {
		return display;
	}

}
