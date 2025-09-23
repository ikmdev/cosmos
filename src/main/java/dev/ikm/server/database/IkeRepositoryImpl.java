package dev.ikm.server.database;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static dev.ikm.tinkar.common.service.CachingService.clearAll;
import static dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator.LOG;

@Repository
public class IkeRepositoryImpl implements IkeRepository {

	private final static Logger LOGGER = LoggerFactory.getLogger(IkeRepositoryImpl.class);

	@Override
	public Latest<ConceptEntityVersion> findLatestConceptById(PublicId publicId, StampCalculator stampCalculator) {
		return stampCalculator.latest(Entity.nid(publicId));
	}

	@Override
	public Optional<ConceptEntity<? extends ConceptEntityVersion>> findConceptById(PublicId publicId) {
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.map(entity -> (ConceptEntity<? extends ConceptEntityVersion>) entity);
	}

	@Override
	public Latest<SemanticEntityVersion> findLatestSemanticById(PublicId publicId, StampCalculator stampCalculator) {
		return stampCalculator.latest(Entity.nid(publicId));
	}

	@Override
	public Optional<SemanticEntity<? extends SemanticEntityVersion>> findSemanticById(PublicId publicId) {
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.map(entity -> (SemanticEntity<? extends SemanticEntityVersion>) entity);
	}

	@Override
	public Latest<PatternEntityVersion> findLatestPatternById(PublicId publicId, StampCalculator stampCalculator) {
		return stampCalculator.latest(Entity.nid(publicId));
	}

	@Override
	public Optional<PatternEntity<? extends PatternEntityVersion>> findPatternById(PublicId publicId) {
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.map(entity -> (PatternEntity<? extends PatternEntityVersion>) entity);
	}

	@Override
	public Latest<StampEntityVersion> findLatestSTAMPById(PublicId publicId, StampCalculator stampCalculator) {
		return stampCalculator.latest(Entity.nid(publicId));
	}

	@Override
	public Optional<StampEntity<? extends StampEntityVersion>> findSTAMPById(PublicId publicId) {
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.map(entity -> (StampEntity<? extends StampEntityVersion>) entity);
	}
}
