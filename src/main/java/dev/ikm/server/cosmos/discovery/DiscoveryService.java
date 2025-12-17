package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import org.springframework.stereotype.Service;

@Service
public class DiscoveryService {

	private final CalculatorService calculatorService;

	public DiscoveryService(CalculatorService calculatorService) {
		this.calculatorService = calculatorService;
	}

}
