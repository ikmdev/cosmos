package dev.ikm.server.cosmos.constellation;

/**
 * Represents the lifecycle phases of a Constellation process, themed around cosmic events.
 */
public enum Phase {

	/**
	 * The initial phase where the constellation's foundation is being laid,
	 * akin to a star or nebula forming from cosmic dust.
	 * Corresponds to the creation of the Neo4j database.
	 */
	QUEUED("Queued"),
	/**
	 * The active processing phase where the constellation is being built and populated,
	 * like charting the stars in the sky.
	 */
	CHARTING("Charting"),
	/**
	 * The final state, indicating that the constellation has been fully processed and is complete.
	 * The constellation is now fully mapped and established.
	 */
	CHARTED("Charted"),

	DELETED("Deleted"),

	/**
	 * The failed state, indicating that the constellation didn't finish it's charting phase.
	 */
	FAILED("Failed");

	private final String display;

	private Phase(String display) {
		this.display = display;
	}

	public String display() {
		return display;
	}
}
