package dev.ikm.server.cosmos.quality;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class QualityController {

	Logger LOG = LoggerFactory.getLogger(QualityController.class);

	@GetMapping("/quality")
	public String getQuality(Model model) {
		model.addAttribute("activePage", "quality");
		model.addAttribute("footerText", "Quality Footer");
		return "quality";
	}

	@HxRequest
	@GetMapping("/quality")
	public FragmentsRendering getQualityWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Quality");
		model.addAttribute("activePage", "quality");
		model.addAttribute("footerText", "Quality Footer");
		return FragmentsRendering
				.with("quality :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}