package dev.ikm.server.cosmos.global;

import dev.ikm.server.cosmos.scope.ScopeService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

	private final ScopeService scopeService;

	public GlobalControllerAdvice(ScopeService scopeService) {
		this.scopeService = scopeService;
	}

	@ModelAttribute
	public void addGlobalAttributes(Model model) {
		// This ensures 'scopes' is available on every page for the navigation dropdown
		model.addAttribute("scopes", scopeService.retrieveAllScopes());

		// You can also handle 'activeScope' here if it's stored in the session
		// or determine it based on the request
	}
}
