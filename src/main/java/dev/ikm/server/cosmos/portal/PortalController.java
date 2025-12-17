package dev.ikm.server.cosmos.portal;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class PortalController {

	Logger LOG = LoggerFactory.getLogger(PortalController.class);

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "portal");
		model.addAttribute("titleDisplayName", "Portal");
		model.addAttribute("footerText", "Gateway to the Cosmos AI");
	}

	@GetMapping("/portal")
	public String getQuality(Model model) {
		addSharedModelAttributes(model);
		return "portal";
	}

	@HxRequest
	@GetMapping("/portal")
	public FragmentsRendering getQualityWithFragments(Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("portal :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}
}