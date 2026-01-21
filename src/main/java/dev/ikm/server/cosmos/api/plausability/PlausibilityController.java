package dev.ikm.server.cosmos.api.plausability;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/plausibility")
public class PlausibilityController {

	private final CalculatorService calculatorService;
	private final PlausibilityService plausibilityService;

	@Autowired
	public PlausibilityController(CalculatorService calculatorService, PlausibilityService plausibilityService) {
		this.calculatorService = calculatorService;
		this.plausibilityService = plausibilityService;
	}

	@GetMapping("/lab-result")
	public PlausibilityResult getLabResultPlausibility(
			@RequestParam("dob") String dob,
			@RequestParam("testCode") String testCode,
			@RequestParam("resultValue") String resultValue,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setScope(stampId, langId, navId);
		return plausibilityService.calculateLabResultPlausibility(dob, testCode, resultValue);
	}

	@GetMapping("/lab-device")
	public PlausibilityResult getLabDevicePlausibility(
			@RequestParam("testCode") String testCode,
			@RequestParam("refRangeLow") String refRangeLow,
			@RequestParam("refRangeHigh") String refRangeHigh,
			@RequestParam("unit") String unit,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setScope(stampId, langId, navId);
		return plausibilityService.calculateLabDevicePlausibility(testCode, refRangeLow, refRangeHigh, unit);
	}
}
