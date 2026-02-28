package dev.ikm.server.cosmos.calculator;

import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.Id;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.server.cosmos.ike.Type;
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

	private UUID observatoryId;
	private StampCoordinateRecord stampCoordinateRecord;
	private LanguageCoordinateRecord languageCoordinateRecord;
	private NavigationCoordinateRecord navigationCoordinateRecord;

	public CalculatorService(ObservatoryRepository observatoryRepository, IkeRepository ikeRepository) {
		this.observatoryRepository = observatoryRepository;
		this.ikeRepository = ikeRepository;
	}

	public void setObservatory(UUID observatoryId) {
		this.observatoryId = observatoryId;
		ObservatoryEntity observatoryEntity = observatoryRepository.readObservatory(observatoryId);
		setObservatory(observatoryEntity.stampCoordinate(), observatoryEntity.languageCoordinate(), observatoryEntity.navigationCoordinate(), observatoryEntity.includedModules(), observatoryEntity.excludedModules());
	}

	public void setObservatory(StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate, NavigationCoordinate navigationCoordinate, List<Facade> includedModules, List<Facade> excludedModules) {
		List<ConceptFacade> include = includedModules.stream()
				.map(facade -> ConceptFacade.make(facade.id().nid()))
				.toList();
		int[] nids = excludedModules.stream()
				.mapToInt(facade -> facade.id().nid())
				.toArray();
		IntIdSet exclude = IntIds.set.of(nids);
//		this.stampCoordinateRecord = stampCoordinate.getRecord()
//				.withModules(include)
//				.withExcludedModuleNids(exclude);

		//TODO-aks8m:
		// Need further testing to see implications for exclude and include of modules in STAMP Coordinate
		// when being used in conjunction with the calculators. Currently to do the Constellation transforms,
		// we need to have the include and exclude (to limit the proliferation of types of knowledge in our knowledge
		// graph schema).
		// The intention was to leverage the Latest<> calculations to help speed up what's "in scope"
		// and "out of scope", but there is still more testing to better understand how Latest<> handles module
		// precedence (for example).
		// There is also just the issue of what parts of the Web App UI should be restricted
		// text (description semantic) wise. Including and excluding can quickly render the UI with out any text (because
		// we can simply exclude or not include the primordial module where Language/Description Type live).
		this.stampCoordinateRecord = stampCoordinate.getRecord();
		this.languageCoordinateRecord = languageCoordinate.getRecord();
		this.navigationCoordinateRecord = navigationCoordinate.getRecord();
	}

	public UUID getObservatoryId() {
		return observatoryId;
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

	public String calculateFQN(int nid) {
		return getLanguageCalculator()
				.getFullyQualifiedNameText(nid)
				.orElse("ERROR: FQN NOT FOUND!");
	}

	public String calculateFQN(Facade facade) {
		return calculateFQN(facade.id().nid());
	}

	public String calculateFQN(PublicId publicId) {
		return calculateFQN(Entity.nid(publicId));
	}

	public String calculateFQN(UUID id) {
		return calculateFQN(PublicIds.of(id));
	}

	public String calculateText(int nid) {
		EntityFacade facade = EntityFacade.make(nid);
		return getLanguageCalculator()
				.getDescriptionText(facade)
				.orElse("ERROR: TEXT NOT FOUND!");
	}

	public String calculateText(Facade facade) {
		return calculateText(facade.id().nid());
	}

	public String calculateText(PublicId publicId) {
		return calculateText(Entity.nid(publicId));
	}

	public String calculateText(UUID id) {
		return calculateText(PublicIds.of(id));
	}

	public String calculateSYN(int nid) {
		return getLanguageCalculator()
				.getRegularDescriptionText(nid)
				.orElse("ERROR: SYN NOT FOUND!");
	}

	public String calculateSYN(Facade facade) {
		return calculateSYN(facade.id().nid());
	}

	public String calculateSYN(PublicId publicId) {
		return calculateSYN(Entity.nid(publicId));
	}

	public String calculateSYN(UUID id) {
		return calculateSYN(PublicIds.of(id));
	}

	public String calculateDEF(int nid) {
		return getLanguageCalculator()
				.getDefinitionDescriptionText(nid)
				.orElse("ERROR: DEF NOT FOUND!");
	}

	public String calculateDEF(Facade facade) {
		return calculateDEF(facade.id().nid());
	}

	public String calculateDEF(PublicId publicId) {
		return calculateDEF(Entity.nid(publicId));
	}

	public String calculateDEF(UUID id) {
		return calculateDEF(PublicIds.of(id));
	}

	private Facade toFacade(PublicId pId) {
		return new Facade(new Id(Entity.nid(pId), pId.asUuidList().castToList()), Type.CONCEPT, calculateText(pId));
	}

	public List<Facade> calculateChildren(int nid) {
		return getNavigationCalculator()
				.childrenOf(nid)
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(this::toFacade)
				.toList();
	}

	public List<Facade> calculateChildren(Facade facade) {
		return calculateChildren(facade.id().nid());
	}

	public List<Facade> calculateChildren(PublicId publicId) {
		return calculateChildren(Entity.nid(publicId));
	}

	public List<Facade> calculateChildren(UUID id) {
		return calculateChildren(PublicIds.of(id));
	}

	public List<Facade> calculateParents(int nid) {
		return getNavigationCalculator()
				.parentsOf(nid)
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(this::toFacade)
				.toList();
	}

	public List<Facade> calculateParents(Facade facade) {
		return calculateParents(facade.id().nid());
	}

	public List<Facade> calculateParents(PublicId publicId) {
		return calculateParents(Entity.nid(publicId));
	}

	public List<Facade> calculateParents(UUID id) {
		return calculateParents(PublicIds.of(id));
	}

	public List<Facade> calculateDescendants(int nid) {
		return getNavigationCalculator()
				.descendentsOf(nid)
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(this::toFacade)
				.toList();
	}

	public List<Facade> calculateDescendants(Facade facade) {
		return calculateDescendants(facade.id().nid());
	}

	public List<Facade> calculateDescendants(PublicId publicId) {
		return calculateDescendants(Entity.nid(publicId));
	}

	public List<Facade> calculateDescendants(UUID id) {
		return calculateDescendants(PublicIds.of(id));
	}

	public List<Facade> calculateAncestors(int nid) {
		return getNavigationCalculator()
				.ancestorsOf(nid)
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(this::toFacade)
				.toList();
	}

	public List<Facade> calculateAncestors(Facade facade) {
		return calculateAncestors(facade.id().nid());
	}

	public List<Facade> calculateAncestors(PublicId publicId) {
		return calculateAncestors(Entity.nid(publicId));
	}

	public List<Facade> calculateAncestors(UUID id) {
		return calculateAncestors(PublicIds.of(id));
	}

	public List<Facade> calculateKinds(int nid) {
		return getNavigationCalculator()
				.kindOf(nid)
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(this::toFacade)
				.toList();
	}

	public List<Facade> calculateKinds(Facade facade) {
		return calculateKinds(facade.id().nid());
	}

	public List<Facade> calculateKinds(PublicId publicId) {
		return calculateKinds(Entity.nid(publicId));
	}

	public List<Facade> calculateKinds(UUID id) {
		return calculateKinds(PublicIds.of(id));
	}

}
