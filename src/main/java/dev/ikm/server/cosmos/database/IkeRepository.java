package dev.ikm.server.cosmos.database;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class IkeRepository {

	private final static Logger LOGGER = LoggerFactory.getLogger(IkeRepository.class);

	public Latest<ConceptEntityVersion> findLatestConceptById(PublicId publicId, StampCalculator stampCalculator) {
		return stampCalculator.latest(Entity.nid(publicId));
	}

	public Optional<ConceptEntity<? extends ConceptEntityVersion>> findConceptById(PublicId publicId) {
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.filter(entity -> entity instanceof ConceptEntity<? extends ConceptEntityVersion>)
				.map(entity -> (ConceptEntity<? extends ConceptEntityVersion>) entity);
	}

	public Latest<SemanticEntityVersion> findLatestSemanticById(PublicId publicId, StampCalculator stampCalculator) {
		return stampCalculator.latest(Entity.nid(publicId));
	}

	public Optional<SemanticEntity<? extends SemanticEntityVersion>> findSemanticById(PublicId publicId) {
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.filter(entity -> entity instanceof SemanticEntity<? extends SemanticEntityVersion>)
				.map(entity -> (SemanticEntity<? extends SemanticEntityVersion>) entity);
	}

	public Latest<PatternEntityVersion> findLatestPatternById(PublicId publicId, StampCalculator stampCalculator) {
		return stampCalculator.latest(Entity.nid(publicId));
	}

	public Optional<PatternEntity<? extends PatternEntityVersion>> findPatternById(PublicId publicId) {
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.filter(entity -> entity instanceof PatternEntity<? extends PatternEntityVersion>)
				.map(entity -> (PatternEntity<? extends PatternEntityVersion>) entity);
	}

	public Latest<StampEntityVersion> findLatestSTAMPById(PublicId publicId, StampCalculator stampCalculator) {
		return stampCalculator.latest(Entity.nid(publicId));
	}

	public Optional<StampEntity<? extends StampEntityVersion>> findSTAMPById(PublicId publicId) {
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.filter(entity -> entity instanceof StampEntity<? extends StampEntityVersion>)
				.map(entity -> (StampEntity<? extends StampEntityVersion>) entity);
	}

	public Optional<String> calculateFQNDescription(PublicId publicId, LanguageCalculator languageCalculator) {
		return languageCalculator.getFullyQualifiedNameText(Entity.nid(publicId));
	}

	public Optional<String> calculateSYNDescription(PublicId publicId, LanguageCalculator languageCalculator) {
		return languageCalculator.getRegularDescriptionText(Entity.nid(publicId));
	}

	public Optional<String> calculateDEFDescription(PublicId publicId, LanguageCalculator languageCalculator) {
		return languageCalculator.getDefinitionDescriptionText(Entity.nid(publicId));
	}

	public List<Optional<Entity<EntityVersion>>> calculateParents(PublicId publicId, NavigationCalculator navigationCalculator) {
		return navigationCalculator.parentsOf(Entity.nid(publicId)).mapToList(Entity::get);
	}

	public List<Optional<Entity<EntityVersion>>> calculateChildren(PublicId publicId, NavigationCalculator navigationCalculator) {
		return navigationCalculator.childrenOf(Entity.nid(publicId)).mapToList(Entity::get);
	}

	public List<Optional<Entity<EntityVersion>>> calculateDescendants(PublicId publicId, NavigationCalculator navigationCalculator) {
		return navigationCalculator.descendentsOf(Entity.nid(publicId)).mapToList(Entity::get);
	}

	public List<Optional<Entity<EntityVersion>>> calculateAncestors(PublicId publicId, NavigationCalculator navigationCalculator) {
		return navigationCalculator.ancestorsOf(Entity.nid(publicId)).mapToList(Entity::get);
	}

	public List<Optional<Entity<EntityVersion>>> calculateKind(PublicId publicId, NavigationCalculator navigationCalculator) {
		return navigationCalculator.kindOf(Entity.nid(publicId)).mapToList(Entity::get);
	}

	public List<Latest<Field<Object>>> calculateLatestFields(PublicId publicId, EntityProxy.Pattern pattern, EntityProxy.Concept fieldMeaning, StampCalculator stampCalculator) {
		List<Latest<Field<Object>>> fields = new ArrayList<>();
		PrimitiveData.get().forEachSemanticNidForComponentOfPattern(
				Entity.nid(publicId),
				pattern.nid(),
				nid -> fields.add(stampCalculator.getFieldForSemanticWithMeaning(nid, fieldMeaning.nid()))
		);
		return fields;
	}
}
