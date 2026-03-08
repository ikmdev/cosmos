package dev.ikm.server.cosmos.portal;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class PortalController {

	Logger LOG = LoggerFactory.getLogger(PortalController.class);

	private void addSharedModelAttributes(Model model) {
		model.addAttribute("activePage", "portal");
		model.addAttribute("titleDisplayName", "Portal");
		model.addAttribute("footerText", "Gateway to the Cosmos AI");
	}

	@GetMapping("/portal")
	public String getQuality(Model model) {
		addSharedModelAttributes(model);
		return "portal";
	}

	@HxRequest
	@GetMapping("/portal")
	public FragmentsRendering getQualityWithFragments(Model model) {
		addSharedModelAttributes(model);
		return FragmentsRendering
				.with("portal :: main-content")
				.fragment("fragments/layout/title :: title-content")
				.fragment("fragments/layout/navigation :: navigation-content")
				.fragment("fragments/layout/footer :: footer-content")
				.build();
	}

	@PostMapping("/portal/chat")
	public String postChatMessage(@RequestParam(name = "message", required = false) String message,
	                              @RequestParam(name = "file", required = false) MultipartFile file,
	                              Model model) {
		boolean hasMessage = message != null && !message.isBlank();
		boolean hasFile = file != null && !file.isEmpty();

		// The client-side script should prevent empty submissions, but we validate again.
		if (!hasMessage && !hasFile) {
			// If a blank message gets through, return the indicator to be swapped with itself,
			// resulting in no visual change for the user.
			return "fragments/portal/chat :: indicator-only";
		}

		// Simulate processing and generating a response
		try {
			// Simulate network/processing delay to show indicator
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			LOG.warn("Chat response delay interrupted", e);
			Thread.currentThread().interrupt();
		}

		StringBuilder responseText = new StringBuilder("I received ");
		if (hasMessage) {
			responseText.append("your message: \"").append(message).append("\"");
		}
		if (hasFile) {
			if (hasMessage) {
				responseText.append(" and ");
			}
			responseText.append("your file: '").append(file.getOriginalFilename()).append("' (size: ").append(file.getSize()).append(" bytes)");
		}
		responseText.append(". I am a simple echo bot for now.");

		model.addAttribute("responseMessage", responseText.toString());

		return "fragments/portal/chat :: bot-response-and-indicator";
	}
}