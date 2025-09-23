package dev.ikm.server.database;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;

import java.util.Optional;

public interface IkeRepository {

	Latest<ConceptEntityVersion> findLatestConceptById(PublicId publicId, StampCalculator stampCalculator);
	Optional<ConceptEntity<? extends ConceptEntityVersion>> findConceptById(PublicId publicId);
	Latest<SemanticEntityVersion> findLatestSemanticById(PublicId publicId, StampCalculator stampCalculator);
	Optional<SemanticEntity<? extends SemanticEntityVersion>> findSemanticById(PublicId publicId);
	Latest<PatternEntityVersion> findLatestPatternById(PublicId publicId, StampCalculator stampCalculator);
	Optional<PatternEntity<? extends PatternEntityVersion>> findPatternById(PublicId publicId);
	Latest<StampEntityVersion> findLatestSTAMPById(PublicId publicId, StampCalculator stampCalculator);
	Optional<StampEntity<? extends StampEntityVersion>> findSTAMPById(PublicId publicId);
}
