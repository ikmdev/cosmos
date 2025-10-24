package dev.ikm.server.cosmos.database;

import dev.ikm.server.cosmos.api.coordinate.LanguageCoord;
import dev.ikm.server.cosmos.api.coordinate.NavigationCoord;
import dev.ikm.server.cosmos.api.coordinate.StampCoord;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
public class Context {

	private StampCoordinateRecord   stampCoordinateRecord;
	private LanguageCoordinateRecord   languageCoordinateRecord;
	private NavigationCoordinateRecord navigationCoordinateRecord;

	public void setViewContext(UUID stampId, UUID languageId, UUID navigationId) {
		this.stampCoordinateRecord = StampCoord.toRecord(stampId);
		this.languageCoordinateRecord = LanguageCoord.toRecord(languageId);
		this.navigationCoordinateRecord = NavigationCoord.toRecord(navigationId);
	}

	public StampCalculator getStampCalculator() {
		return StampCalculatorWithCache.getCalculator(stampCoordinateRecord);
	}

	public LanguageCalculator getLanguageCalculator() {
		return LanguageCalculatorWithCache.getCalculator(stampCoordinateRecord, Lists.immutable.of(languageCoordinateRecord));
	}

	public NavigationCalculator getNavigationCalculator() {
		return NavigationCalculatorWithCache.getCalculator(stampCoordinateRecord, Lists.immutable.of(languageCoordinateRecord), navigationCoordinateRecord);
	}
}
