package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
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
import org.springframework.web.servlet.view.FragmentsRendering;

import java.util.UUID;

@Controller
public class ConstellationController {

	private final Logger LOG = LoggerFactory.getLogger(ConstellationController.class);

	private final ConstellationService constellationService;
	private final CalculatorService calculatorService;

	@Autowired
	public ConstellationController(ConstellationService constellationService, CalculatorService calculatorService) {
		this.constellationService = constellationService;
		this.calculatorService = calculatorService;
	}

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "constellation");
		model.addAttribute("titleDisplayName", "Constellation");
		model.addAttribute("footerText", "Mapping the stars of knowledge");
		model.addAttribute("constellationForm", new ConstellationForm(""));
		model.addAttribute("constellations", constellationService.retrieveAllConstellations());
	}

	@GetMapping("/constellation")
	public String getConstellation(Model model) {
		addSharedModelAttributes(model);
		return "constellation";
	}

	@HxRequest
	@GetMapping("/constellation")
	public FragmentsRendering getConstellationWithFragments(Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("constellation :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}

	@HxRequest
	@PostMapping("/constellation")
	public FragmentsRendering postConstellation(
			@ModelAttribute("activeScopeId") UUID activeScopeId,
			@ModelAttribute("constellationForm") ConstellationForm constellationForm,
			Model model) {
		if (activeScopeId != null) {
			calculatorService.setScope(activeScopeId);
		}
		Constellation newConstellation = constellationService.saveNewConstellation(constellationForm);
		model.addAttribute("newConstellation", newConstellation);
		return FragmentsRendering
				.with("fragments/constellation/constellation-table-row :: new-constellation-row")
				.build();
	}

	@HxRequest
	@DeleteMapping("/constellation/{id}")
	public String deleteConstellation(@PathVariable("id") UUID id, Model model) {
		constellationService.removeConstellation(id);
		return "";
	}
}
