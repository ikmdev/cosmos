package dev.ikm.server.cosmos.global;

import dev.ikm.server.cosmos.scope.ScopeDTO;
import dev.ikm.server.cosmos.scope.ScopeService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.UUID;

@ControllerAdvice
public class GlobalControllerAdvice {

	private final ScopeService scopeService;

	public GlobalControllerAdvice(ScopeService scopeService) {
		this.scopeService = scopeService;
	}

	@ModelAttribute("scopes")
	public List<ScopeDTO> addScopesToModel() {
		return scopeService.retrieveAllScopes();
	}

	@ModelAttribute("activeScopeId")
	public UUID addScopeSelectionToModel(
			@CookieValue(name = "cosmos-scope-id", required = false) String scopeSelectionId) {
		if (scopeSelectionId == null) {
			return null;
		}
		try {
			return UUID.fromString(scopeSelectionId);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
