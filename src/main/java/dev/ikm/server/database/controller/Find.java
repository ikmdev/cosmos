package dev.ikm.server.database.controller;

import dev.ikm.server.database.entity.ConceptChronologyView;
import dev.ikm.server.database.entity.PatternChronologyView;
import dev.ikm.server.database.entity.StampChronologyView;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import org.eclipse.collections.api.factory.Lists;

import java.util.Optional;
import java.util.UUID;

public class Find {

	public ConceptChronologyView conceptChronologyView(UUID uuid, UUID languageCoordinate) {
		PublicId publicId = PublicIds.of(uuid);
		int nid = Entity.nid(publicId);
		return conceptChronologyView(nid, languageCoordinate);
	}

	public ConceptChronologyView conceptChronologyView(int nid, UUID languageCoordinate) {
		Transform transform = getTransform(languageCoordinate);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(nid);
		if (optionalEntity.isEmpty()) {
			return new ConceptChronologyView(nid, null, null, null);
		}
		Entity<? extends EntityVersion> entity = optionalEntity.get();
		ConceptEntity<? extends ConceptEntityVersion> conceptEntity = (ConceptEntity<? extends ConceptEntityVersion>) entity;
		return transform.conceptChronology(conceptEntity);
	}

	public PatternChronologyView patternChronology(UUID uuid, UUID languageCoordinates) {
		PublicId publicId = PublicIds.of(uuid);
		int nid = Entity.nid(publicId);
		return patternChronology(nid, languageCoordinates);
	}

	public PatternChronologyView patternChronology(int nid, UUID languageCoordinates) {
		Transform transform = getTransform(languageCoordinates);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(nid);
		if (optionalEntity.isEmpty()){
			return new PatternChronologyView(nid, null, null);
		}
		Entity<? extends EntityVersion> entity = optionalEntity.get();
		PatternEntity<? extends PatternEntityVersion> patternEntity = (PatternEntity<? extends PatternEntityVersion>) entity;
		return transform.patternChronology(patternEntity);
	}

	public StampChronologyView stampChronology(UUID uuid, UUID languageCoordinates) {
		PublicId publicId = PublicIds.of(uuid);
		int nid = Entity.nid(publicId);
		return stampChronology(nid, languageCoordinates);
	}

	public StampChronologyView stampChronology(int nid, UUID languageCoordinates) {
		Transform transform = getTransform(languageCoordinates);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(nid);
		if (optionalEntity.isEmpty()) {
			return new StampChronologyView(nid, null, null);
		}
		Entity<? extends EntityVersion> entity = optionalEntity.get();
		StampEntity<? extends StampEntityVersion> stampEntity = (StampEntity<? extends StampEntityVersion>) entity;
		return transform.stampChronology(stampEntity);
	}



	private Transform getTransform(UUID languageCoordinate) {
		if (languageCoordinate == null) {
			return new Transform(PrimitiveData::textFast);
		}
		//This will get replace once there's starter data for common view coordinate permutations
		if (languageCoordinate.equals(UUID.fromString("07347499-784e-4f50-9379-c00fde8ba86a"))) {
			final LanguageCalculatorWithCache languageCalculatorWithCache = LanguageCalculatorWithCache.getCalculator(
					Coordinates.Stamp.DevelopmentLatest(),
					Lists.immutable.of(Coordinates.Language.UsEnglishFullyQualifiedName())
			);
			return new Transform(languageCalculatorWithCache::getDescriptionTextOrNid);
		} else if (languageCoordinate.equals(UUID.fromString("0221506d-2bbf-4750-aa65-68bdcb690e08"))) {
			final LanguageCalculatorWithCache languageCalculatorWithCache = LanguageCalculatorWithCache.getCalculator(
					Coordinates.Stamp.DevelopmentLatest(),
					Lists.immutable.of(Coordinates.Language.UsEnglishRegularName())
			);
			return new Transform(languageCalculatorWithCache::getDescriptionTextOrNid);
		}
		return new Transform(PrimitiveData::textFast);
	}
}
