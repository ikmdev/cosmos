package dev.ikm.server.cosmos.constellation;

import java.util.Set;
import java.util.UUID;

import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;

public record Chart(Action action, UUID constellationId, Set<Facade> scopes,
					Set<Facade> includedModules,
					Set<Facade> excludedModules,
					StampCalculator stampCalculator,
					LanguageCalculator languageCalculator, NavigationCalculator navigationCalculator) {
}