package dev.ikm.server.cosmos.constellations;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class ConstellationsController {

	Logger LOG = LoggerFactory.getLogger(ConstellationsController.class);

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "constellations");
		model.addAttribute("titleDisplayName", "Constellations");
		model.addAttribute("footerText", "Mapping the stars of knowledge");
	}

	@GetMapping("/constellations")
	public String getKnowledge(Model model) {
		addSharedModelAttributes(model);
		return "constellations";
	}

	@HxRequest
	@GetMapping("/constellations")
	public FragmentsRendering getKnowledgeWithFragments(Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("constellations :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}
}
