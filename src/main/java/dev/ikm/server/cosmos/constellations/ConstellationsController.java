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

	@GetMapping("/constellations")
	public String getKnowledge(Model model) {
		model.addAttribute("activePage", "constellations");
		model.addAttribute("footerText", "Mapping the stars of knowledge");
		return "constellations";
	}

	@HxRequest
	@GetMapping("/constellations")
	public FragmentsRendering getKnowledgeWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Constellations");
		model.addAttribute("activePage", "constellations");
		model.addAttribute("footerText", "Mapping the stars of knowledge");
		return FragmentsRendering
				.with("constellations :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}
