package dev.ikm.server.cosmos.synthesize;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class SynthesizeController {

	Logger LOG = LoggerFactory.getLogger(SynthesizeController.class);

	@GetMapping("/synthesize")
	public String getQuality(Model model) {
		model.addAttribute("activePage", "synthesize");
		model.addAttribute("footerText", "Distilling insights from complexity");
		return "synthesize";
	}

	@HxRequest
	@GetMapping("/synthesize")
	public FragmentsRendering getQualityWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Synthesize");
		model.addAttribute("activePage", "synthesize");
		model.addAttribute("footerText", "Distilling insights from complexity");
		return FragmentsRendering
				.with("synthesize :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}