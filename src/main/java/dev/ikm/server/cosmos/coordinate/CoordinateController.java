package dev.ikm.server.cosmos.coordinate;

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
		for(LanguageCoord languageCoord : LanguageCoord.values()) {
			coordinates.add(new CoordinateDTO(languageCoord.getName(), languageCoord.getUuids()));
		}
		return coordinates;
	}

	@GetMapping("/navigation")
	public List<CoordinateDTO> getNavigationCoordinates() {
		List<CoordinateDTO> coordinates = new ArrayList<>();
		for(NavigationCoord navigationCoord : NavigationCoord.values()) {
			coordinates.add(new CoordinateDTO(navigationCoord.getName(), navigationCoord.getUuids()));
		}
		return coordinates;
	}

	@GetMapping("/stamp")
	public List<CoordinateDTO> getStampCoordinates() {
		List<CoordinateDTO> coordinates = new ArrayList<>();
		for(StampCoord stampCoord : StampCoord.values()) {
			coordinates.add(new CoordinateDTO(stampCoord.getName(), stampCoord.getUuids()));
		}
		return coordinates;
	}
}
