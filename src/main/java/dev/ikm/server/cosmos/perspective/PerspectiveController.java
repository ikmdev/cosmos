package dev.ikm.server.cosmos.perspective;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class PerspectiveController {

	@GetMapping("/perspective")
	public String getPerspectivePage(Model model) {
		return "/layouts/perspective";
	}
}
