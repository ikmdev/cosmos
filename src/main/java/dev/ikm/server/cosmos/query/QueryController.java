package dev.ikm.server.cosmos.query;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class QueryController {

	@GetMapping("/query")
	public String getQueryPage() {
		return "/layouts/query";
	}

}
