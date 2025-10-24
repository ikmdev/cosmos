package dev.ikm.server.cosmos.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class HomeController {

	@GetMapping("/home")
	public String getHomePage(Model model) {
		// For full page refreshes, decorate the layout with home.html
		return "/layouts/home";
	}
}
