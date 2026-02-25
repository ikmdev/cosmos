package dev.ikm.server.cosmos.global;

import dev.ikm.server.cosmos.observatory.Observatory;
import dev.ikm.server.cosmos.observatory.ObservatoryDatabaseConfig;
import dev.ikm.server.cosmos.observatory.ObservatoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.UUID;

@ControllerAdvice
public class GlobalControllerAdvice {

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
		return observatoryService.retrieveAllObservatories().orElse(List.of());
	}

	@ModelAttribute("activeObservatoryId")
	public UUID addObservatorySelectionToModel(HttpServletRequest request) {
		return (UUID) request.getAttribute(CalculatorServiceInterceptor.ACTIVE_OBSERVATORY_ID_ATTR);
	}
}
