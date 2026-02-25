package dev.ikm.server.cosmos.ike;

public enum Type {

	ENTITY("Entity"),
	CONCEPT("Concept"),
	SEMANTIC("Semantic"),
	PATTERN("Pattern"),
	STAMP("Stamp"),
	COORDINATE("Coordinate");

	private final String name;

	Type(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
