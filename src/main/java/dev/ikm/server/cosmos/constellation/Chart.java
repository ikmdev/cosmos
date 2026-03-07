package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record Chart(Action action, UUID constellationId, UUID observatoryId, Set<Facade> scopes, StampCalculator stampCalculator,
					LanguageCalculator languageCalculator, NavigationCalculator navigationCalculator) {
}