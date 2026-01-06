package dev.ikm.server.cosmos.api.semantic;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/semantic")
public class SemanticController {

	private final SemanticService semanticService;
	private final CalculatorService calculatorService;

	@Autowired
	public SemanticController(SemanticService semanticService, CalculatorService calculatorService) {
		this.semanticService = semanticService;
		this.calculatorService = calculatorService;
	}

	@GetMapping("/{uuid}")
	public SemanticChronologyDTO getSemanticWithAllVersions(
			@PathVariable("uuid") UUID uuid) {
		SemanticChronologyDTO s = semanticService.retrieveSemanticWithAllVersions(uuid);
		return semanticService.retrieveSemanticWithAllVersions(uuid);
	}

	@GetMapping("/latest/{uuid}")
	public SemanticChronologyDTO getSemanticWithLatestVersion(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setScope(stampId, langId, navId);
		return semanticService.retrieveSemanticWithLatestVersion(uuid);
	}

	@GetMapping("/{uuid}/us-dialect")
	public List<UUID> getUSDialectSemantic(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setScope(stampId, langId, navId);
		return semanticService.calculateUSDialect(uuid);
	}

	@GetMapping("/{uuid}/gb-dialect")
	public List<UUID> getGBDialectSemantic(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setScope(stampId, langId, navId);
		return semanticService.calculateGBDialect(uuid);
	}

	@GetMapping("/{uuid}/semantics")
	public List<List<UUID>> getSemantics(
			@PathVariable("uuid") UUID uuid,
			@RequestParam(value = "meaning", required = false) UUID meaningID) {
		return semanticService.retrieveSemantics(uuid);
	}
}
