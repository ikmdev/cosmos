package dev.ikm.server.database.entity;

public record StampVersionView(
	int statusNid,
	PublicIdView statusPublicIdView,
	String statusDescription,
	long time,
	String formattedTime,
	int authorNid,
	PublicIdView authorPublicIdView,
	String authorDescription,
	int moduleNid,
	PublicIdView modulePublicIdView,
	String moduleDescription,
	int pathNid,
	PublicIdView pathPublicIdView,
	String pathDescription) {
}
