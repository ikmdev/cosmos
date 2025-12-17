package dev.ikm.server.cosmos.rules;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class RulesController {

	Logger LOG = LoggerFactory.getLogger(RulesController.class);

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "rules");
		model.addAttribute("titleDisplayName", "Rules");
		model.addAttribute("footerText", "Automation and logic management");
	}

	@GetMapping("/rules")
	public String getQuery(Model model) {
		addSharedModelAttributes(model);
		return "rules";
	}

	@HxRequest
	@GetMapping("/rules")
	public FragmentsRendering getQueryWithFragments(Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("rules :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}
}
