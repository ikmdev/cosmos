package dev.ikm.server.cosmos.discovery;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class DiscoveryController {

	Logger LOG = LoggerFactory.getLogger(DiscoveryController.class);

	@GetMapping("/discovery")
	public String getData(Model model) {
		model.addAttribute("activePage", "discovery");
		model.addAttribute("footerText", "Navigate your knowledge universe");
		return "discovery";
	}

	@HxRequest
	@GetMapping("/discovery")
	public FragmentsRendering getDataWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Discovery");
		model.addAttribute("activePage", "discovery");
		model.addAttribute("footerText", "Navigate your knowledge universe");
		return FragmentsRendering
				.with("discovery :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}
