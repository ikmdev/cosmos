package dev.ikm.server.cosmos.data;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class DataController {

	Logger LOG = LoggerFactory.getLogger(DataController.class);

	@GetMapping("/data")
	public String getData(Model model) {
		model.addAttribute("activePage", "data");
		model.addAttribute("footerText", "Data Footer");
		return "data";
	}

	@HxRequest
	@GetMapping("/data")
	public FragmentsRendering getDataWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Data");
		model.addAttribute("activePage", "data");
		model.addAttribute("footerText", "Data Footer");
		return FragmentsRendering
				.with("data :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}
