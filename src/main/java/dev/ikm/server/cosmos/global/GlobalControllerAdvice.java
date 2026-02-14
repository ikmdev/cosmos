package dev.ikm.server.cosmos.global;

import dev.ikm.server.cosmos.observatory.Observatory;
import dev.ikm.server.cosmos.observatory.ObservatoryDatabaseConfig;
import dev.ikm.server.cosmos.observatory.ObservatoryService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.UUID;

@ControllerAdvice
public class GlobalControllerAdvice {

	private final ObservatoryService observatoryService;

	public GlobalControllerAdvice(ObservatoryService observatoryService) {
		this.observatoryService = observatoryService;
	}

	@ModelAttribute("observatories")
	public List<Observatory> addObservatoriesToModel() {
		return observatoryService.retrieveAllObservatories();
	}

	@ModelAttribute("activeObservatoryId")
	public UUID addObservatorySelectionToModel(
			@CookieValue(name = "cosmos-observatory-id", required = false) String observatorySelectionId) {
		if (observatorySelectionId == null) {
			return ObservatoryDatabaseConfig.DEFAULT_OBSERVATORY_ID;
		}
		try {
			return UUID.fromString(observatorySelectionId);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
