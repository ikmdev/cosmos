package dev.ikm.server.cosmos.constellation;

import java.util.Set;

import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.observatory.StringToFacade;

public record ConstellationForm(
		String name,
		@StringToFacade Set<Facade> selectedIncludedScopes,
		String portalPrompt) {
}
