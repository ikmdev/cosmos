package dev.ikm.server.cosmos.constellation;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class ConstellationController {

	Logger LOG = LoggerFactory.getLogger(ConstellationController.class);

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "constellation");
		model.addAttribute("titleDisplayName", "Constellation");
		model.addAttribute("footerText", "Mapping the stars of knowledge");
	}

	@GetMapping("/constellation")
	public String getKnowledge(Model model) {
		addSharedModelAttributes(model);
		return "constellation";
	}

	@HxRequest
	@GetMapping("/constellation")
	public FragmentsRendering getKnowledgeWithFragments(Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("constellation :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}
}
