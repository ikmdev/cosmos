package dev.ikm.server.cosmos.view;

import dev.ikm.server.cosmos.api.coordinate.LanguageCoord;
import dev.ikm.server.cosmos.api.coordinate.NavigationCoord;
import dev.ikm.server.cosmos.api.coordinate.StampCoord;

public record ViewDTO(StampCoord stamp,
					  LanguageCoord lang,
					  NavigationCoord nav) { }
