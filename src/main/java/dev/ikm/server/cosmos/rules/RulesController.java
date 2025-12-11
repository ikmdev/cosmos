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

	@GetMapping("/rules")
	public String getQuery(Model model) {
		model.addAttribute("activePage", "rules");
		model.addAttribute("footerText", "Automation and logic management");
		return "rules";
	}

	@HxRequest
	@GetMapping("/rules")
	public FragmentsRendering getQueryWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Rules");
		model.addAttribute("activePage", "rules");
		model.addAttribute("footerText", "Automation and logic management");
		return FragmentsRendering
				.with("rules :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}
