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
	public List<CoordinateDTO> getLanguageCoordinates() {
		List<CoordinateDTO> coordinates = new ArrayList<>();
		for(Language language : Language.values()) {
			coordinates.add(new CoordinateDTO(language.getName(), language.getUuids()));
		}
		return coordinates;
	}

	@GetMapping("/navigation")
	public List<CoordinateDTO> getNavigationCoordinates() {
		List<CoordinateDTO> coordinates = new ArrayList<>();
		for(Navigation navigation : Navigation.values()) {
			coordinates.add(new CoordinateDTO(navigation.getName(), navigation.getUuids()));
		}
		return coordinates;
	}

	@GetMapping("/stamp")
	public List<CoordinateDTO> getStampCoordinates() {
		List<CoordinateDTO> coordinates = new ArrayList<>();
		for(Stamp stamp : Stamp.values()) {
			coordinates.add(new CoordinateDTO(stamp.getName(), stamp.getUuids()));
		}
		return coordinates;
	}
}
