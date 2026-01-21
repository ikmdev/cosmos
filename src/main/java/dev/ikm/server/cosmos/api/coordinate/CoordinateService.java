package dev.ikm.server.cosmos.api.coordinate;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CoordinateService {

	public Optional<Stamp> stampCoordinate(List<UUID> ids) {
		for (Stamp stamp : Stamp.values()) {
			for (UUID id : ids) {
				if (stamp.getUuids().contains(id)) {
					return Optional.of(stamp);
				}
			}
		}
		return Optional.empty();
	}

	public Coordinate stampCoordinate(Stamp stamp) {
		return new Coordinate(stamp.getName(), stamp.getUuids());
	}

	public List<Coordinate> stampCoordinates() {
		return Arrays.stream(Stamp.values())
				.map(stamp -> new Coordinate(stamp.getName(), stamp.getUuids()))
				.toList();
	}

	public Optional<Language> languageCoordinate(List<UUID> ids) {
		for (Language language : Language.values()) {
			for (UUID id : ids) {
				if (language.getUuids().contains(id)) {
					return Optional.of(language);
				}
			}
		}
		return Optional.empty();
	}

	public Coordinate languageCoordinate(Language language) {
		return new Coordinate(language.getName(), language.getUuids());
	}

	public List<Coordinate> languageCoordinates() {
		return Arrays.stream(Language.values())
				.map(language -> new Coordinate(language.getName(), language.getUuids()))
				.toList();
	}

	public Optional<Navigation> navigationCoordinate(List<UUID> ids) {
		for (Navigation navigation : Navigation.values()) {
			for (UUID id : ids) {
				if (navigation.getUuids().contains(id)) {
					return Optional.of(navigation);
				}
			}
		}
		return Optional.empty();
	}

	public Coordinate navigationCoordinate(Navigation navigation) {
		return new Coordinate(navigation.getName(), navigation.getUuids());
	}

	public List<Coordinate> navigationCoordinates() {
		return Arrays.stream(Navigation.values())
				.map(navigation -> new Coordinate(navigation.getName(), navigation.getUuids()))
				.toList();
	}

}
