package dev.ikm.server.database;

import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import org.eclipse.collections.api.factory.Lists;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequestScope
public class ViewContext {

	private final static Map<UUID, StampCalculator> stampCalcCache = new HashMap<>();
	private final static Map<UUID, LanguageCalculator> langCalcCache = new HashMap<>();
	private StampCalculator stampCalculator;
	private LanguageCalculator languageCalculator;

	public StampCalculator getStampContext() {
		return stampCalculator;
	}

	public void setSTAMPContext(UUID stampCoordinateId) {
		if (stampCalcCache.containsKey(stampCoordinateId)) {
			this.stampCalculator = stampCalcCache.get(stampCoordinateId);
		}
		StampCoordinateRecord stampCoordinateRecord = Coordinates.Stamp.DevelopmentLatest().toStampCoordinateRecord();
		StampCalculator stampCalculator = StampCalculatorWithCache.getCalculator(stampCoordinateRecord);
		stampCalcCache.put(stampCoordinateId, stampCalculator);
		this.stampCalculator = stampCalculator;
	}

	public LanguageCalculator getLanguageContext() {
		return languageCalculator;
	}

	public void setLanguageContext(UUID languageCoordinateId) {
		if (langCalcCache.containsKey(languageCoordinateId)) {
			this.languageCalculator = langCalcCache.get(languageCoordinateId);
		} else if (languageCoordinateId == null) {
			this.languageCalculator = LanguageCalculatorWithCache.getCalculator(
					Coordinates.Stamp.DevelopmentLatest(),
					Lists.immutable.of(
							Coordinates.Language.AnyLanguageFullyQualifiedName(),
							Coordinates.Language.AnyLanguageRegularName())
			);
		}else if (languageCoordinateId.equals(UUID.fromString("07347499-784e-4f50-9379-c00fde8ba86a"))) {
			this.languageCalculator = LanguageCalculatorWithCache.getCalculator(
					Coordinates.Stamp.DevelopmentLatest(),
					Lists.immutable.of(Coordinates.Language.UsEnglishFullyQualifiedName())
			);
		} else if (languageCoordinateId.equals(UUID.fromString("0221506d-2bbf-4750-aa65-68bdcb690e08"))) {
			this.languageCalculator = LanguageCalculatorWithCache.getCalculator(
					Coordinates.Stamp.DevelopmentLatest(),
					Lists.immutable.of(Coordinates.Language.UsEnglishRegularName())
			);
		} else {
			this.languageCalculator = LanguageCalculatorWithCache.getCalculator(
					Coordinates.Stamp.DevelopmentLatest(),
					Lists.immutable.of(
							Coordinates.Language.AnyLanguageFullyQualifiedName(),
							Coordinates.Language.AnyLanguageRegularName())
			);
		}

		langCalcCache.put(languageCoordinateId, this.languageCalculator);
	}
}
