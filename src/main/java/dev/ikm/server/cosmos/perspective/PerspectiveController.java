package dev.ikm.server.cosmos.perspective;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class PerspectiveController {

	Logger LOG = LoggerFactory.getLogger(PerspectiveController.class);

	@GetMapping("/perspective")
	public String getPerspective(Model model) {
		model.addAttribute("activePage", "perspective");
		model.addAttribute("footerText", "Perspective Footer");
		return "perspective";
	}

	@HxRequest
	@GetMapping("/perspective")
	public FragmentsRendering getPerspectiveWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Perspective");
		model.addAttribute("activePage", "perspective");
		model.addAttribute("footerText", "Perspective Footer");
		return FragmentsRendering
				.with("perspective :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}
