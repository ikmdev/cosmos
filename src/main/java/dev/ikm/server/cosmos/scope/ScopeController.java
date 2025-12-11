package dev.ikm.server.cosmos.scope;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class ScopeController {

	Logger LOG = LoggerFactory.getLogger(ScopeController.class);

	@GetMapping("/scope")
	public String getPerspective(Model model) {
		model.addAttribute("activePage", "scope");
		model.addAttribute("footerText", "Defining your field of view");
		return "scope";
	}

	@HxRequest
	@GetMapping("/scope")
	public FragmentsRendering getPerspectiveWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Scope");
		model.addAttribute("activePage", "scope");
		model.addAttribute("footerText", "Defining your field of view");
		return FragmentsRendering
				.with("scope :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}
