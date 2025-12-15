package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.Language;
import dev.ikm.server.cosmos.api.coordinate.Navigation;
import dev.ikm.server.cosmos.api.coordinate.Stamp;

public record ScopeEntity(String name,
						  Stamp stamp,
						  Language language,
						  Navigation navigation) {
}
