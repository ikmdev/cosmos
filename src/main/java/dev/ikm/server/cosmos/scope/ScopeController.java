package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.CoordinateService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.util.UUID;

@Controller
public class ScopeController {

	Logger LOG = LoggerFactory.getLogger(ScopeController.class);

	private final CoordinateService coordinateService;
	private final ScopeService scopeService;


	@Autowired
	public ScopeController(CoordinateService coordinateService, ScopeService scopeService) {
		this.coordinateService = coordinateService;
		this.scopeService = scopeService;
	}

	@GetMapping("/scope")
	public String getScope(Model model) {
		//Minimum needed data to fully render page
		model.addAttribute("activePage", "scope");
		model.addAttribute("footerText", "Defining your field of view");

		//Add to model the enumerations for Stamp, Language, and Navigation coordinates
		ScopeFormDTO scopeFormDTO = new ScopeFormDTO(
				"",
				coordinateService.stampCoordinates(),
				coordinateService.languageCoordinates(),
				coordinateService.navigationCoordinates(),
				null,
				null,
				null);
		model.addAttribute("scopeForm", scopeFormDTO);
		model.addAttribute("scopes", scopeService.retrieveAllScopes());
		return "scope";
	}

	@HxRequest
	@GetMapping("/scope")
	public FragmentsRendering getScopeWithFragments(Model model) {
		//Minimum needed data to fully render page
		model.addAttribute("titleDisplayName", "Scope");
		model.addAttribute("activePage", "scope");
		model.addAttribute("footerText", "Defining your field of view");

		//Add to model the enumerations for Stamp, Language, and Navigation coordinates
		ScopeFormDTO scopeFormDTO = new ScopeFormDTO(
				"",
				coordinateService.stampCoordinates(),
				coordinateService.languageCoordinates(),
				coordinateService.navigationCoordinates(),
				null,
				null,
				null);
		model.addAttribute("scopeForm", scopeFormDTO);
		model.addAttribute("scopes", scopeService.retrieveAllScopes());
		return FragmentsRendering
				.with("scope :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}

	@HxRequest
	@PostMapping("/scope")
	public FragmentsRendering postScope(@ModelAttribute("scopeForm") ScopeFormDTO scopeFormDTO, Model model) {
		//Create a new Scope
		ScopeDTO newScope = scopeService.saveNewScope(
				scopeFormDTO.name(),
				scopeFormDTO.selectedStampCoordinateId(),
				scopeFormDTO.selectedLanguageCoordinateId(),
				scopeFormDTO.selectedNavigationCoordinateId());
		model.addAttribute("newScope", newScope);
		return FragmentsRendering
				.with("fragments/scope/scope-table-row :: new-scope-row")
				.build();
	}

	@HxRequest
	@DeleteMapping("/scope/{id}")
	@ResponseBody
	public String deleteScope(@PathVariable("id") UUID id) {
		scopeService.removeScope(id);
		return "";
	}
}
