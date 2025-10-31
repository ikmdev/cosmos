package dev.ikm.server.cosmos.query;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class QueryController {

	Logger LOG = LoggerFactory.getLogger(QueryController.class);

	@GetMapping("/query")
	public String getQuery(Model model) {
		model.addAttribute("activePage", "query");
		model.addAttribute("footerText", "Query Footer");
		return "query";
	}

	@HxRequest
	@GetMapping("/query")
	public FragmentsRendering getQueryWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Query");
		model.addAttribute("activePage", "query");
		model.addAttribute("footerText", "Query Footer");
		return FragmentsRendering
				.with("query :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}
