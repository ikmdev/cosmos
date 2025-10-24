package dev.ikm.server.cosmos.knowledge;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KnowledgeController {

	@GetMapping("/knowledge")
	public String getKnowledgePage() {
		return "/layouts/knowledge";
	}

}
