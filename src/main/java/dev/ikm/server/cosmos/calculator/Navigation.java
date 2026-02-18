package dev.ikm.server.cosmos.calculator;

import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.Id;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.entity.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public enum Navigation {

	INFERRED("Inferred Navigation", List.of(UUID.fromString("10f727e4-adac-4a94-80f5-00614692aa46")), Coordinates.Navigation.inferred().toNavigationCoordinateRecord()),
	STATED("Stated Navigation", List.of(UUID.fromString("2ea11ae5-d715-48aa-839c-84e27fa5394c")), Coordinates.Navigation.inferred().toNavigationCoordinateRecord()),;

	private final String name;
	private final List<UUID> uuids;
	private final NavigationCoordinateRecord record;
	private final Facade facade;

	Navigation(String name, List<UUID> uuids, NavigationCoordinateRecord record) {
		this.name = name;
		this.uuids = uuids;
		this.record = record;
		PublicId publicId = PublicIds.of(uuids);
		int nid = Entity.nid(publicId);
		this.facade = new Facade(new Id(nid, this.uuids), this.name);
	}

	public String getName() {
		return this.name;
	}

	public List<UUID> getUuids() {
		return this.uuids;
	}

	public NavigationCoordinateRecord getRecord() {
		return this.record;
	}

	public static NavigationCoordinateRecord toRecord(UUID uuid) {
		for (Navigation coord : Navigation.values()) {
			if (coord.uuids.contains(uuid)) {
				return coord.getRecord();
			}
		}
		return INFERRED.getRecord();
	}

	public static Navigation fromId(Id id) {
		for (Navigation navigation : values()) {
			if (navigation.getConcept().id().equals(id)) {
				return navigation;
			}
		}
		throw new RuntimeException("Navigation not found");
	}

	public Facade getConcept() {
		return this.facade;
	}

	public static List<Facade> navigationConcepts() {
		return Arrays.stream(values())
				.map(Navigation::getConcept)
				.toList();
	}

	public int getNid() {
		PublicId publicId = PublicIds.of(uuids);
		return Entity.nid(publicId);
	}
}
