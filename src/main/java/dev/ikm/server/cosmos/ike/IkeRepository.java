package dev.ikm.server.cosmos.ike;

import dev.ikm.server.cosmos.api.calculator.CalculatorService;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
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
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTermV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class IkeRepository {

	private final static Logger LOG = LoggerFactory.getLogger(IkeRepository.class);

	private final IkeDatabaseConfig ikeDatabaseConfig;
	private final CalculatorService calculatorService;

	@Autowired
	public IkeRepository(IkeDatabaseConfig ikeDatabaseConfig, CalculatorService calculatorService) {
		this.ikeDatabaseConfig = ikeDatabaseConfig;
		this.calculatorService = calculatorService;
	}

	public List<UUID> getIds(UUID uuid) {
		return PublicIds.of(uuid).asUuidList().castToList();
	}

	public List<UUID> getIds(Entity<?> entity) {
		return entity.publicId().asUuidList().castToList();
	}

	public List<UUID> getIds(EntityFacade entityFacade) {
		return entityFacade.publicId().asUuidList().castToList();
	}

	public List<UUID> getIds(State state) {
		return state.publicId().asUuidList().castToList();
	}

	public Latest<ConceptEntityVersion> findLatestConceptById(UUID id) {
		PublicId publicId = PublicIds.of(id);
		return calculatorService.getStampCalculator().latest(Entity.nid(publicId));
	}

	public Optional<ConceptEntity<? extends ConceptEntityVersion>> findConceptById(UUID id) {
		PublicId publicId = PublicIds.of(id);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.filter(entity -> entity instanceof ConceptEntity<? extends ConceptEntityVersion>)
				.map(entity -> (ConceptEntity<? extends ConceptEntityVersion>) entity);
	}

	public Latest<SemanticEntityVersion> findLatestSemanticById(UUID id) {
		PublicId publicId = PublicIds.of(id);
		return calculatorService.getStampCalculator().latest(Entity.nid(publicId));
	}

	public Optional<SemanticEntity<? extends SemanticEntityVersion>> findSemanticById(UUID id) {
		PublicId publicId = PublicIds.of(id);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.filter(entity -> entity instanceof SemanticEntity<? extends SemanticEntityVersion>)
				.map(entity -> (SemanticEntity<? extends SemanticEntityVersion>) entity);
	}

	public Latest<PatternEntityVersion> findLatestPatternById(UUID id) {
		PublicId publicId = PublicIds.of(id);
		return calculatorService.getStampCalculator().latest(Entity.nid(publicId));
	}

	public Optional<PatternEntity<? extends PatternEntityVersion>> findPatternById(UUID id) {
		PublicId publicId = PublicIds.of(id);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.filter(entity -> entity instanceof PatternEntity<? extends PatternEntityVersion>)
				.map(entity -> (PatternEntity<? extends PatternEntityVersion>) entity);
	}

	public Latest<StampEntityVersion> findLatestSTAMPById(UUID id) {
		PublicId publicId = PublicIds.of(id);
		return calculatorService.getStampCalculator().latest(Entity.nid(publicId));
	}

	public Optional<StampEntity<? extends StampEntityVersion>> findSTAMPById(UUID id) {
		PublicId publicId = PublicIds.of(id);
		Optional<Entity<EntityVersion>> optionalEntity = Entity.get(Entity.nid(publicId));
		return optionalEntity
				.map(entity -> (Entity<? extends EntityVersion>) entity)
				.filter(entity -> entity instanceof StampEntity<? extends StampEntityVersion>)
				.map(entity -> (StampEntity<? extends StampEntityVersion>) entity);
	}

	public List<List<UUID>> findAssociatedSemanticIds(UUID id) {
		List<List<UUID>> semanticIds = new ArrayList<>();
		PublicId conceptPublicId = PublicIds.of(id);
		ikeDatabaseConfig.getPrimitiveDataService().forEachSemanticNidForComponent(
				Entity.nid(conceptPublicId),
				semanticNid -> semanticIds.add(PrimitiveData.publicId(semanticNid).asUuidList().toList()));
		return semanticIds;
	}

	public Map<List<UUID>, String> findIdentifiers(UUID id) {
		Map<List<UUID>, String> identifierMap = new HashMap<>();
		PublicId conceptPublicId = PublicIds.of(id);
		ikeDatabaseConfig.getPrimitiveDataService().forEachSemanticNidForComponentOfPattern(
				Entity.nid(conceptPublicId),
				TinkarTermV2.IDENTIFIER_PATTERN.nid(),
				identifierSemanticNid -> {
					Latest<Field<PublicId>> latestSource = calculatorService
							.getStampCalculator()
							.getFieldForSemanticWithMeaning(identifierSemanticNid, TinkarTermV2.IDENTIFIER_SOURCE);
					Latest<Field<String>> latestValue = calculatorService
							.getStampCalculator()
							.getFieldForSemanticWithMeaning(identifierSemanticNid, TinkarTermV2.IDENTIFIER_VALUE);
					if (latestSource.isPresent() && latestValue.isPresent()) {
						identifierMap.put(latestSource.get().value().asUuidList().stream().toList(), latestValue.get().value());
					}
				}
		);
		return identifierMap;
	}

	public List<UUID> findUSDialect(UUID id) {
		return findAcceptability(id, TinkarTermV2.US_DIALECT_PATTERN);
	}

	public List<UUID> findGBDialect(UUID id) {
		return findAcceptability(id, TinkarTermV2.GB_DIALECT_PATTERN);
	}

	private List<UUID> findAcceptability(UUID id, EntityProxy.Pattern dialect) {
		List<UUID> acceptabilityId = new ArrayList<>();
		PublicId semanticPublicId = PublicIds.of(id);
		ikeDatabaseConfig.getPrimitiveDataService().forEachSemanticNidForComponentOfPattern(
				Entity.nid(semanticPublicId),
				dialect.nid(),
				nid -> {
					Latest<Field<PublicId>> acceptability = calculatorService.getStampCalculator()
							.getFieldForSemanticWithMeaning(nid, TinkarTermV2.GREAT_BRITAIN_ENGLISH_DIALECT);
					if (acceptability.isPresent()) {
						acceptabilityId.addAll(acceptability.get().value().asUuidList().stream().toList());
					}
				}
		);
		return acceptabilityId;
	}
}
