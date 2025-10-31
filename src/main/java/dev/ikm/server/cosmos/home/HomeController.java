package dev.ikm.server.cosmos.home;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class HomeController {

	Logger LOG = LoggerFactory.getLogger(HomeController.class);

	@GetMapping("/home")
	public String getHome(Model model) {
		model.addAttribute("activePage", "home");
		model.addAttribute("footerText", "Home Footer");
		return "home";
	}

	@HxRequest
	@GetMapping("/home")
	public FragmentsRendering getHomeWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Home");
		model.addAttribute("activePage", "home");
		model.addAttribute("footerText", "Home Footer");
		return FragmentsRendering
				.with("home :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}
