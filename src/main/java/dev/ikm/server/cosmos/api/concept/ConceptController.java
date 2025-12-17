package dev.ikm.server.cosmos.api.concept;

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
@RequestMapping("/api/concept")
public class ConceptController {

	private final ConceptService conceptService;
	private final CalculatorService calculatorService;

	@Autowired
	public ConceptController(ConceptService conceptService, CalculatorService calculatorService) {
		this.conceptService = conceptService;
		this.calculatorService = calculatorService;
	}

	@GetMapping("/{uuid}")
	public ConceptChronologyDTO getConceptWithAllVersions(
			@PathVariable("uuid") UUID uuid) {
		return conceptService.retrieveConceptWithAllVersions(uuid);
	}

	@GetMapping("/latest/{uuid}")
	public ConceptChronologyDTO getConceptWithLatestVersion(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setViewContext(stampId, langId, navId);
		return conceptService.retrieveConceptWithLatestVersion(uuid);
	}

	@GetMapping("/{uuid}/fqn")
	public String getConceptFQN(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setViewContext(stampId, langId, navId);
		return conceptService.retrieveFullyQualifiedName(uuid);
	}

	@GetMapping("/{uuid}/syn")
	public String getConceptSYN(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setViewContext(stampId, langId, navId);
		return conceptService.retrieveSynonym(uuid);
	}

	@GetMapping("/{uuid}/def")
	public String getConceptDEF(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setViewContext(stampId, langId, navId);
		return conceptService.retrieveDefinition(uuid);
	}

	@GetMapping("/{uuid}/children")
	public List<List<UUID>> getConceptChildren(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setViewContext(stampId, langId, navId);
		return conceptService.retrieveChildren(uuid);
	}

	@GetMapping("/{uuid}/parents")
	public List<List<UUID>> getConceptParents(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setViewContext(stampId, langId, navId);
		return conceptService.retrieveParents(uuid);
	}

	@GetMapping("/{uuid}/descendants")
	public List<List<UUID>> getConceptDescendants(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setViewContext(stampId, langId, navId);
		return conceptService.retrieveDescendants(uuid);
	}

	@GetMapping("/{uuid}/ancestors")
	public List<List<UUID>> getConceptAncestors(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setViewContext(stampId, langId, navId);
		return conceptService.retrieveAncestors(uuid);
	}

	@GetMapping("/{uuid}/kinds")
	public List<List<UUID>> getConceptKinds(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setViewContext(stampId, langId, navId);
		return conceptService.retrieveKinds(uuid);
	}

	@GetMapping("/{uuid}/identifiers")
	public List<IdentifierSemanticDTO> getConceptIdentifiers(
			@PathVariable("uuid") UUID uuid,
			@RequestParam("stamp") UUID stampId,
			@RequestParam("lang") UUID langId,
			@RequestParam("nav") UUID navId) {
		calculatorService.setViewContext(stampId, langId, navId);
		return conceptService.retrieveIdentifiers(uuid);
	}

	@GetMapping("/{uuid}/semantics")
	public List<List<UUID>> getSemanticsForConceptChronology(
			@PathVariable("uuid") UUID uuid,
			@RequestParam(value = "meaning", required = false) UUID meaningID) { //TODO
		return conceptService.retrieveSemantics(uuid);
	}

}
