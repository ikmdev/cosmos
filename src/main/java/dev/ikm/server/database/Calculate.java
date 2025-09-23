package dev.ikm.server.database;

import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.TinkarTermV2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Calculate {

	private final StampCalculator stampCalculator;

	public Calculate() {
		StampCoordinateRecord stampCoordinateRecord = Coordinates.Stamp.DevelopmentLatest().toStampCoordinateRecord();
		this.stampCalculator = StampCalculatorWithCache.getCalculator(stampCoordinateRecord);
	}

	public boolean plausibility(UUID uuid, List<String> parameters) {
		PublicId samPattern = PublicIds.of(uuid);
		int samPatternNid = Entity.nid(samPattern);
		for (List<String> knowledgeIdentifiers : buildPluasabilityMap(samPatternNid).values()) {
			if (new HashSet<>(knowledgeIdentifiers).containsAll(parameters)) {
				return true;
			}
		}
		return false;
	}

	private Map<Integer, List<String>> buildPluasabilityMap(int samPatternNid) {
		Map<Integer, List<String>> plausabilityMap = new HashMap<>();
		PrimitiveData.get().forEachSemanticNidOfPattern(samPatternNid, semanticNid -> {
			Latest<SemanticEntityVersion> latestPlausibilitySemantic = stampCalculator.latest(semanticNid);
			if (latestPlausibilitySemantic.isPresent()) {
				int referencedComponentNid = latestPlausibilitySemantic.get().referencedComponentNid();
				List<String> identifiersFromKnowledgeBase = identifierValuesForComponent(referencedComponentNid);
				identifiersFromKnowledgeBase.addAll(identifiersFromPlausibilitySAM(semanticNid));
				plausabilityMap.put(referencedComponentNid, identifiersFromKnowledgeBase);
			}
		});
		return plausabilityMap;
	}

	private List<String> identifierValuesForComponent(int componentNid) {
		final List<String> identifiers = new ArrayList<>();
		PrimitiveData.get().forEachSemanticNidForComponentOfPattern(
				componentNid,
				TinkarTermV2.IDENTIFIER_PATTERN.nid(),
				identifierNid -> {
					Latest<Field<String>> latestField = stampCalculator.getFieldForSemanticWithMeaning(identifierNid, TinkarTermV2.IDENTIFIER_VALUE);
					if (latestField.isPresent()) {
						identifiers.add(latestField.get().value());
					}
				}
		);
		return identifiers;
	}

	private List<String> identifiersFromPlausibilitySAM(int samSemanticNid) {
		final List<String> identifiers = new ArrayList<>();
		PublicId samMeaning = PublicIds.of(UUID.fromString("e29dae30-5039-52ad-9ed6-65ee5c1ca3d9"));
		int samMeaningNid = Entity.nid(samMeaning);
		Latest<Field<IntIdSet>> latestIntIdSet = stampCalculator.getFieldForSemanticWithMeaning(samSemanticNid, Entity.getFast(samMeaningNid));
		if (latestIntIdSet.isPresent()) {
			latestIntIdSet.get().value().forEach(nid -> identifiers.addAll(identifierValuesForComponent(nid)));
		}
		return identifiers;
	}
}
