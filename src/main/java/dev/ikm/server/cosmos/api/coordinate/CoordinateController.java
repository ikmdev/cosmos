package dev.ikm.server.cosmos.api.coordinate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/coordinate")
public class CoordinateController {

	@GetMapping("/language")
	public List<Coordinate> getLanguageCoordinates() {
		List<Coordinate> coordinates = new ArrayList<>();
		for(Language language : Language.values()) {
			coordinates.add(new Coordinate(language.getName(), language.getUuids()));
		}
		return coordinates;
	}

	@GetMapping("/navigation")
	public List<Coordinate> getNavigationCoordinates() {
		List<Coordinate> coordinates = new ArrayList<>();
		for(Navigation navigation : Navigation.values()) {
			coordinates.add(new Coordinate(navigation.getName(), navigation.getUuids()));
		}
		return coordinates;
	}

	@GetMapping("/stamp")
	public List<Coordinate> getStampCoordinates() {
		List<Coordinate> coordinates = new ArrayList<>();
		for(Stamp stamp : Stamp.values()) {
			coordinates.add(new Coordinate(stamp.getName(), stamp.getUuids()));
		}
		return coordinates;
	}
}
