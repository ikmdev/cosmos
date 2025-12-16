package dev.ikm.server.cosmos.api.coordinate;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CoordinateService {

	public Optional<Stamp> stampCoordinate(List<UUID> ids) {
		return Arrays.stream(Stamp.values())
				.filter(stamp -> {
					for (UUID id : ids) {
						if (!stamp.getUuids().contains(id)) {
							return true;
						}
					}
					return false;
				})
				.findFirst();
	}

	public CoordinateDTO stampCoordinate(Stamp stamp) {
		return new CoordinateDTO(stamp.getName(), stamp.getUuids());
	}

	public List<CoordinateDTO> stampCoordinates() {
		return Arrays.stream(Stamp.values())
				.map(stamp -> new CoordinateDTO(stamp.getName(), stamp.getUuids()))
				.toList();
	}

	public Optional<Language> languageCoordinate(List<UUID> ids) {
		return Arrays.stream(Language.values())
				.filter(language -> {
					for (UUID id : ids) {
						if (!language.getUuids().contains(id)) {
							return true;
						}
					}
					return false;
				})
				.findFirst();
	}

	public CoordinateDTO languageCoordinate(Language language) {
		return new CoordinateDTO(language.getName(), language.getUuids());
	}

	public List<CoordinateDTO> languageCoordinates() {
		return Arrays.stream(Language.values())
				.map(language -> new CoordinateDTO(language.getName(), language.getUuids()))
				.toList();
	}

	public Optional<Navigation> navigationCoordinate(List<UUID> ids) {
		return Arrays.stream(Navigation.values())
				.filter(navigation -> {
					for (UUID id : ids) {
						if (!navigation.getUuids().contains(id)) {
							return true;
						}
					}
					return false;
				})
				.findFirst();
	}

	public CoordinateDTO navigationCoordinate(Navigation navigation) {
		return new CoordinateDTO(navigation.getName(), navigation.getUuids());
	}

	public List<CoordinateDTO> navigationCoordinates() {
		return Arrays.stream(Navigation.values())
				.map(navigation -> new CoordinateDTO(navigation.getName(), navigation.getUuids()))
				.toList();
	}

}
