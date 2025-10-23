package dev.ikm.server.cosmos.webapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

	@GetMapping("/view")
	public String home(Model model) {
		return "fragments/view :: view";
	}
}
