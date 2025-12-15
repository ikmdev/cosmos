package dev.ikm.server.cosmos.api.coordinate;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class CoordinateService {

	public List<CoordinateDTO> stampCoordinates() {
		return Arrays.stream(Stamp.values())
				.map(stamp -> new CoordinateDTO(stamp.getName(), stamp.getUuids()))
				.toList();
	}

	public List<CoordinateDTO> languageCoordinates() {
		return Arrays.stream(Language.values())
				.map(language -> new CoordinateDTO(language.getName(), language.getUuids()))
				.toList();
	}

	public List<CoordinateDTO> navigationCoordinates() {
		return Arrays.stream(Navigation.values())
				.map(navigation -> new CoordinateDTO(navigation.getName(), navigation.getUuids()))
				.toList();
	}

	public Stamp buildStamp(UUID id) {
		for(Stamp stamp : Stamp.values()) {
			if (stamp.getUuids().contains(id)) {
				return stamp;
			}
		}
		return null;
	}

}
