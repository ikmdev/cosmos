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

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "query");
		model.addAttribute("titleDisplayName", "Query");
		model.addAttribute("footerText", "Precise knowledge retrieval");
	}

	@GetMapping("/query")
	public String getQuery(Model model) {
		addSharedModelAttributes(model);
		return "query";
	}

	@HxRequest
	@GetMapping("/query")
	public FragmentsRendering getQueryWithFragments(Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("query :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}
}
