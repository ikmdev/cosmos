package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.CoordinateService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class ScopeController {

	Logger LOG = LoggerFactory.getLogger(ScopeController.class);

	private final CoordinateService coordinateService;

	@Autowired
	public ScopeController(CoordinateService coordinateService) {
		this.coordinateService = coordinateService;
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
		return FragmentsRendering
				.with("scope :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}

	@HxRequest
	@PostMapping("/scope")
	public FragmentsRendering createScope(@ModelAttribute("scopeForm") ScopeFormDTO scopeFormDTO, Model model) {
		return FragmentsRendering.with("scope :: main-content")
				.build();
	}

}
