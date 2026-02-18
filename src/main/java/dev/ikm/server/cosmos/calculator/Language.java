package dev.ikm.server.cosmos.calculator;

import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.Id;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.entity.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public enum Language {
	ANY_LANG_REG("Any Language Regular Name", List.of(UUID.fromString("456ed121-e492-403e-8448-351a4645b7cd")), Coordinates.Language.AnyLanguageRegularName()),
	ANY_LANG_FQN("Any Language Fully Qualified Name", List.of(UUID.fromString("f4b46e0b-9622-452f-89f2-64e4dab8ef37")), Coordinates.Language.AnyLanguageFullyQualifiedName()),
	ANY_LANG_DEF("Any Language Definition", List.of(UUID.fromString("1b46224b-91c1-4bfe-8428-a1e895649216")), Coordinates.Language.AnyLanguageDefinition()),
	US_ENG_REG("US English Language Regular Name", List.of(UUID.fromString("05df10d8-88c2-440c-a3c0-a286f14b4cd7")), Coordinates.Language.UsEnglishRegularName()),
	US_ENG_FQN("US English Language Fully Qualified Name", List.of(UUID.fromString("60d9dd49-e379-4fa7-8801-53fdc5be34aa")), Coordinates.Language.UsEnglishFullyQualifiedName()),
	GB_ENG_REG("GB English Language Regular Name", List.of(UUID.fromString("8a812d6e-8de1-4405-917e-89d04ae5208a")), Coordinates.Language.GbEnglishPreferredName()),
	GB_ENG_FQN("GB English Language Fully Qualified Name", List.of(UUID.fromString("ac82d1e6-1d1e-4695-a72f-c97f0b651f01")), Coordinates.Language.GbEnglishFullyQualifiedName()),
	ESP_REG("Spanish Language Regular Name", List.of(UUID.fromString("7333bbe8-0857-48cd-8479-0cdbf4f8f831")), Coordinates.Language.SpanishPreferredName()),
	ESP_FQN("Spanish Language Fully Qualified Name", List.of(UUID.fromString("c79e417c-d061-46fd-addd-de2cec751299")), Coordinates.Language.SpanishFullyQualifiedName()),;

	private final Facade facade;
	private final String name;
	private final List<UUID> uuids;
	private final LanguageCoordinateRecord record;

	Language(String name, List<UUID> uuids, LanguageCoordinateRecord record) {
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

	public LanguageCoordinateRecord getRecord() {
		return this.record;
	}

	public static LanguageCoordinateRecord toRecord(UUID uuid) {
		for (Language coord : Language.values()) {
			if (coord.uuids.contains(uuid)) {
				return coord.getRecord();
			}
		}
		return US_ENG_REG.getRecord();
	}

	public static Language fromId(Id id) {
		for (Language language : values()) {
			if (language.getConcept().id().equals(id)) {
				return language;
			}
		}
		throw new RuntimeException("Language not found");
	}

	public Facade getConcept() {
		return this.facade;
	}

	public static List<Facade> languageConcepts() {
		return Arrays.stream(values())
				.map(Language::getConcept)
				.toList();
	}

	public int getNid() {
		PublicId publicId = PublicIds.of(uuids);
		return Entity.nid(publicId);
	}
}
