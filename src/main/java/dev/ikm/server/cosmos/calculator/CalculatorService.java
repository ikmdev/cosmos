package dev.ikm.server.cosmos.calculator;

import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.Id;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.server.cosmos.observatory.ObservatoryEntity;
import dev.ikm.server.cosmos.observatory.ObservatoryRepository;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculatorWithCache;
import dev.ikm.tinkar.coordinate.navigation.NavigationCoordinateRecord;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculatorWithCache;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.UUID;

@Service
@RequestScope
public class CalculatorService {

	private final ObservatoryRepository observatoryRepository;
	private final IkeRepository ikeRepository;

	private StampCoordinateRecord stampCoordinateRecord;
	private LanguageCoordinateRecord languageCoordinateRecord;
	private NavigationCoordinateRecord navigationCoordinateRecord;

	public CalculatorService(ObservatoryRepository observatoryRepository, IkeRepository ikeRepository) {
		this.observatoryRepository = observatoryRepository;
		this.ikeRepository = ikeRepository;
	}

	public void setObservatory(UUID observatoryId) {
		ObservatoryEntity observatoryEntity = observatoryRepository.readObservatory(observatoryId);
		setObservatory(observatoryEntity.stamp(), observatoryEntity.language(), observatoryEntity.navigation(), observatoryEntity.includedModules(), observatoryEntity.excludedModules());
	}

	public void setObservatory(Stamp stamp, Language language, Navigation navigation, List<List<UUID>> includedModules, List<List<UUID>> excludedModules) {
		List<ConceptFacade> include = makeConceptFacadeList(includedModules);
		IntIdSet exclude = makeIntIdSet(excludedModules);
		this.stampCoordinateRecord = stamp.getRecord()
				.withModules(include)
				.withExcludedModuleNids(exclude);

		this.languageCoordinateRecord = language.getRecord();
		this.navigationCoordinateRecord = navigation.getRecord();
	}

	private List<ConceptFacade> makeConceptFacadeList(List<List<UUID>> publicIds) {
		return publicIds.stream()
				.map(PublicIds::of)
				.map(Entity::nid)
				.map(ConceptFacade::make)
				.toList();
	}

	private IntIdSet makeIntIdSet(List<List<UUID>> publicIds) {
		int[] nids = publicIds.stream()
				.map(PublicIds::of)
				.mapToInt(Entity::nid)
				.toArray();
		return IntIds.set.of(nids);
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

	public String calculateFQN(PublicId publicId) {
		return getLanguageCalculator()
				.getFullyQualifiedNameText(Entity.nid(publicId))
				.orElse("");
	}

	public String calculateFQN(UUID id) {
		return calculateFQN(PublicIds.of(id));
	}

	public String calculateText(PublicId publicId) {
		EntityFacade facade = ikeRepository.getEntityFacade(publicId);
		return getLanguageCalculator()
				.getDescriptionText(facade)
				.orElse("");
	}

	public String calculateText(UUID id) {
		return calculateText(PublicIds.of(id));
	}


	public String calculateSYN(PublicId publicId) {
		return getLanguageCalculator()
				.getRegularDescriptionText(Entity.nid(publicId))
				.orElse("");
	}

	public String calculateSYN(UUID id) {
		return calculateSYN(PublicIds.of(id));
	}

	public String calculateDEF(PublicId publicId) {
		return getLanguageCalculator()
				.getDefinitionDescriptionText(Entity.nid(publicId))
				.orElse("");
	}

	public String calculateDEF(UUID id) {
		return calculateDEF(PublicIds.of(id));
	}

	public List<Facade> calculateChildren(PublicId publicId) {
		return getNavigationCalculator()
				.childrenOf(Entity.nid(publicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(pId -> new Facade(new Id(Entity.nid(pId), pId.asUuidList().castToList()), calculateText(pId)))
				.toList();
	}

	public List<Facade> calculateChildren(UUID id) {
		return calculateChildren(PublicIds.of(id));
	}

	public List<Facade> calculateParents(PublicId publicId) {
		return getNavigationCalculator()
				.parentsOf(Entity.nid(publicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(pId -> new Facade(new Id(Entity.nid(pId), pId.asUuidList().castToList()), calculateText(pId)))
				.toList();
	}

	public List<Facade> calculateParents(UUID id) {
		return calculateParents(PublicIds.of(id));
	}

	public List<Facade> calculateDescendants(PublicId publicId) {
		return getNavigationCalculator()
				.descendentsOf(Entity.nid(publicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(pId -> new Facade(new Id(Entity.nid(pId), pId.asUuidList().castToList()), calculateText(pId)))
				.toList();
	}

	public List<Facade> calculateDescendants(UUID id) {
		return calculateDescendants(PublicIds.of(id));
	}

	public List<Facade> calculateAncestors(PublicId publicId) {
		return getNavigationCalculator()
				.ancestorsOf(Entity.nid(publicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(pId -> new Facade(new Id(Entity.nid(pId), pId.asUuidList().castToList()), calculateText(pId)))
				.toList();
	}

	public List<Facade> calculateAncestors(UUID id) {
		return calculateAncestors(PublicIds.of(id));
	}

	public List<Facade> calculateKinds(PublicId publicId) {
		return getNavigationCalculator()
				.kindOf(Entity.nid(publicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(pId -> new Facade(new Id(Entity.nid(pId), pId.asUuidList().castToList()), calculateText(pId)))
				.toList();
	}

	public List<Facade> calculateKinds(UUID id) {
		return calculateKinds(PublicIds.of(id));
	}

}
