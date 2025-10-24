package dev.ikm.server.cosmos.data;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DataController {

	@GetMapping("/data")
	public String getDataPage(Model model) {
		return "/layouts/data";
	}
}
