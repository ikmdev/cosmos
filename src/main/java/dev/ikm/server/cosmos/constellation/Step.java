package dev.ikm.server.cosmos.constellation;

public enum Step {

	WRITE_CONCEPTS("Writing Concept Knowledge"),
	WRITE_HIERARCHY("Writing Hierarchy Knowledge"),
	WRITE_LOGICAL_DEFINITIONS("Writing Logical Definition Knowledge"),
	WRITE_SEMANTICS("Writing Semantic Knowledge");

	private String display;

	private Step(String display) {
		this.display = display;
	}

	public String getDisplay() {
		return display;
	}

}
