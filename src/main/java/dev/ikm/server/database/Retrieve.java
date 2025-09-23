package dev.ikm.server.database;

import dev.ikm.server.rest.concept.ConceptChronologyDTO;
import dev.ikm.server.rest.pattern.PatternChronologyView;
import dev.ikm.server.rest.semantic.SemanticChronologyView;
import dev.ikm.server.rest.stamp.StampChronologyView;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class Retrieve {
//
//	private final TransformService transformService;
//
//	public Retrieve(UUID languageCoordinateUUID, UUID stampCoordinateUUID, boolean includeHistory, boolean isRecursive) {
//		StampCalculator stampCalculator;
//		LanguageCalculator languageCalculator = null;
//
//
//
//
//		this.transformService = new TransformService(stampCalculator, languageCalculator, includeHistory, isRecursive);
//	}
//
//
//	public ConceptChronologyDTO conceptChronology(UUID uuid) {
//		Entity<? extends EntityVersion> entity = findEntity(uuid);
//		ConceptEntity<? extends ConceptEntityVersion> conceptEntity = (ConceptEntity<? extends ConceptEntityVersion>) entity;
//		return transformService.conceptEntity(conceptEntity);
//	}
//
//	public SemanticChronologyView semanticChronology(UUID uuid) {
//		Entity<? extends EntityVersion> entity = findEntity(uuid);
//		SemanticEntity<? extends SemanticEntityVersion> semanticEntity = (SemanticEntity<? extends SemanticEntityVersion>) entity;
//		return transformService.semanticChronology(semanticEntity);
//	}
//
//	public PatternChronologyView patternChronology(UUID uuid) {
//		Entity<? extends EntityVersion> entity = findEntity(uuid);
//		PatternEntity<? extends PatternEntityVersion> patternEntity = (PatternEntity<? extends PatternEntityVersion>) entity;
//		return transformService.patternChronology(patternEntity);
//	}
//
//	public StampChronologyView stampChronology(UUID uuid) {
//		Entity<? extends EntityVersion> entity = findEntity(uuid);
//		StampEntity<? extends StampEntityVersion> stampEntity = (StampEntity<? extends StampEntityVersion>) entity;
//		return transformService.stampChronology(stampEntity);
//	}
}
