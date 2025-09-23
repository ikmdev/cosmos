package dev.ikm.server.rest.concept;

import dev.ikm.server.database.ViewContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ConceptController {

	private final ConceptService conceptService;
	private final ViewContext viewContext;

	@Autowired
	public ConceptController(ConceptService conceptService, ViewContext viewContext) {
		this.conceptService = conceptService;
		this.viewContext = viewContext;
	}

	@GetMapping("/concept/{uuid}")
	public ConceptChronologyDTO getConceptChronology2(
			@PathVariable("uuid") UUID uuid,
			@RequestParam(value = "stamp", required = false) UUID stampCoordinateUUID,
			@RequestParam(value = "language", required = false) UUID languageCoordinateUUID) {
		viewContext.setSTAMPContext(stampCoordinateUUID);
		viewContext.setLanguageContext(languageCoordinateUUID);
		return conceptService.retrieveConcept(uuid);
	}
}
