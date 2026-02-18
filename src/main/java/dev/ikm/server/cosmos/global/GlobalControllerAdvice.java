package dev.ikm.server.cosmos.global;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.observatory.Observatory;
import dev.ikm.server.cosmos.observatory.ObservatoryDatabaseConfig;
import dev.ikm.server.cosmos.observatory.ObservatoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.UUID;

@ControllerAdvice
public class GlobalControllerAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerAdvice.class);

	private final ObservatoryService observatoryService;

	public GlobalControllerAdvice(ObservatoryService observatoryService) {
		this.observatoryService = observatoryService;
	}

	@ModelAttribute("defaultObservatory")
	public UUID addDefaultObservatoryToModel() {
		return ObservatoryDatabaseConfig.DEFAULT_OBSERVATORY_ID;
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
		} else {
			try {
				return UUID.fromString(observatorySelectionId);
			} catch (IllegalArgumentException e) {
				LOG.warn("Invalid UUID in 'cosmos-observatory-id' cookie: '{}'. Falling back to default.", observatorySelectionId);
				return ObservatoryDatabaseConfig.DEFAULT_OBSERVATORY_ID;
			}
		}
	}
}
