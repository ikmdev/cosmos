package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.LanguageCoord;
import dev.ikm.server.cosmos.api.coordinate.NavigationCoord;
import dev.ikm.server.cosmos.api.coordinate.StampCoord;

public record ScopeDTO(StampCoord stamp,
					   LanguageCoord lang,
					   NavigationCoord nav) { }
