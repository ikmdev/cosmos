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

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "home");
		model.addAttribute("titleDisplayName", "Home");
		model.addAttribute("footerText", "Explore your knowledge universe");
	}

	@GetMapping("/home")
	public String getHome(Model model) {
		addSharedModelAttributes(model);
		return "home";
	}

	@HxRequest
	@GetMapping("/home")
	public FragmentsRendering getHomeWithFragments(Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("home :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}
}
