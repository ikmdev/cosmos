package dev.ikm.server.cosmos.webapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	@GetMapping("/")
	public String home(Model model) {
		// Add an attribute to the model to be used by the Thymeleaf template
		model.addAttribute("message", "Hello from Thymeleaf!");

		// Return the name of the template to be rendered
		return "index";
	}
}
