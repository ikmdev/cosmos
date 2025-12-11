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

	@GetMapping("/portal")
	public String getQuality(Model model) {
		model.addAttribute("activePage", "portal");
		model.addAttribute("footerText", "Gateway to the Cosmos AI");
		return "portal";
	}

	@HxRequest
	@GetMapping("/portal")
	public FragmentsRendering getQualityWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Portal");
		model.addAttribute("activePage", "portal");
		model.addAttribute("footerText", "Gateway to the Cosmos AI");
		return FragmentsRendering
				.with("portal :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}