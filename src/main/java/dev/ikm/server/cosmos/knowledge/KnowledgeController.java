package dev.ikm.server.cosmos.knowledge;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class KnowledgeController {

	Logger LOG = LoggerFactory.getLogger(KnowledgeController.class);

	@GetMapping("/knowledge")
	public String getKnowledge(Model model) {
		model.addAttribute("activePage", "knowledge");
		model.addAttribute("footerText", "Knowledge Footer");
		return "knowledge";
	}

	@HxRequest
	@GetMapping("/knowledge")
	public FragmentsRendering getKnowledgeWithFragments(Model model) {
		model.addAttribute("titleDisplayName", "Knowledge");
		model.addAttribute("activePage", "knowledge");
		model.addAttribute("footerText", "Knowledge Footer");
		return FragmentsRendering
				.with("knowledge :: main-content")
				.fragment("fragments/title :: title-content")
				.fragment("fragments/navigation :: navigation-content")
				.fragment("fragments/footer :: footer-content")
				.build();
	}
}
