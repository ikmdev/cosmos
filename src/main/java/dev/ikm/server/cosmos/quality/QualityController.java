package dev.ikm.server.cosmos.quality;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class QualityController {

	@GetMapping("/quality")
	public String getQualityPage() {
		return "/layouts/quality";
	}

}
