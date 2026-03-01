
package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.calculator.LanguageCoordinate;
import dev.ikm.server.cosmos.calculator.NavigationCoordinate;
import dev.ikm.server.cosmos.calculator.StampCoordinate;
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.Id;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.server.cosmos.ike.Type;
import dev.ikm.server.cosmos.search.SearchService;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.TinkarTermV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.ikm.server.cosmos.observatory.ObservatoryDatabaseConfig.DEFAULT_OBSERVATORY_ID;

@Service
public class ObservatoryService {

	private final ObservatoryRepository observatoryRepository;
	private final CalculatorService calculatorService;
	private final IkeRepository ikeRepository;
	private final SearchService searchService;

	@Autowired
	public ObservatoryService(ObservatoryRepository observatoryRepository, CalculatorService calculatorService, IkeRepository ikeRepository, SearchService searchService) {
		this.observatoryRepository = observatoryRepository;
		this.calculatorService = calculatorService;
		this.ikeRepository = ikeRepository;
		this.searchService = searchService;
	}

	private Observatory buildObservatory(ObservatoryEntity observatoryEntity) {
		return new Observatory(
				observatoryEntity.id(),
				observatoryEntity.name(),
				observatoryEntity.stampCoordinate().getConcept(),
				observatoryEntity.languageCoordinate().getConcept(),
				observatoryEntity.navigationCoordinate().getConcept(),
				observatoryEntity.includedModules(),
				observatoryEntity.excludedModules(),
				observatoryEntity.includeScopes());
	}

	private ObservatoryEntity buildObservatoryEntity(Observatory observatory) {
		StampCoordinate stampCoordinate = StampCoordinate.fromId(observatory.stampCoordinate().id());
		LanguageCoordinate languageCoordinate = LanguageCoordinate.fromId(observatory.languageCoordinate().id());
		NavigationCoordinate navigationCoordinate = NavigationCoordinate.fromId(observatory.navigationCoordinate().id());
		return new ObservatoryEntity(observatory.id(), Instant.now(), observatory.name(),
				stampCoordinate,
				languageCoordinate,
				navigationCoordinate,
				observatory.includedModules(),
				observatory.excludedModules(),
				observatory.includedScopes());
	}

	public void bootstrapDefaultObservatory() {
		ObservatoryEntity defaultObservatory = new ObservatoryEntity(
				DEFAULT_OBSERVATORY_ID,
				Instant.now(),
				"Default Observatory",
				StampCoordinate.DEV_LATEST,
				LanguageCoordinate.US_ENG_REG,
				NavigationCoordinate.INFERRED,
				List.of(),
				List.of(),
				List.of());
		observatoryRepository.createObservatory(defaultObservatory);
	}

	public Observatory saveNewObservatory(ObservatoryForm observatoryForm) {
		UUID id = UUID.randomUUID();
		//TODO - Handle the edge case where the coordinate ids aren't correct
		Observatory observatory = new Observatory(
				id,
				observatoryForm.name(),
				observatoryForm.selectedStampCoordinate(),
				observatoryForm.selectedLanguageCoordinate(),
				observatoryForm.selectedNavigationCoordinate(),
				observatoryForm.selectedIncludedModules(),
				observatoryForm.selectedExcludedModules(),
				observatoryForm.selectedIncludedScopes());
		ObservatoryEntity entity = buildObservatoryEntity(observatory);
		observatoryRepository.createObservatory(entity);
		return observatory;
	}

	public Optional<Observatory> retrieveObservatory(UUID id) {
		ObservatoryEntity observatoryEntity = observatoryRepository.readObservatory(id);
		return Optional.of(buildObservatory(observatoryEntity));
	}

	public Optional<List<Observatory>> retrieveAllObservatories() {
		return Optional.of(observatoryRepository.readAll().stream()
				.sorted(Comparator.comparing(ObservatoryEntity::modified).reversed())
				.map(this::buildObservatory)
				.toList());
	}

	public void removeObservatory(UUID id) {
		observatoryRepository.deleteObservatory(id);
	}

	public void updateObservatory(Observatory observatory) {
		ObservatoryEntity entity = buildObservatoryEntity(observatory);
		observatoryRepository.updateObservatory(observatory.id(), entity);
	}

	public Optional<List<Facade>> retrieveModules() {
		List<PublicId> publicIds = ikeRepository.findAllModules();
		return Optional.of(publicIds.stream()
				.map(publicId -> new Facade(new Id(Entity.nid(publicId), Arrays.asList(publicId.asUuidArray())), Type.CONCEPT, calculatorService.calculateText(publicId)))
				.toList());
	}

	public Optional<List<Facade>> retrieveStamps() {
		return Optional.of(StampCoordinate.stampConcepts());
	}

	public Optional<List<Facade>> retrieveLanguages() {
		return Optional.of(LanguageCoordinate.languageConcepts());
	}

	public Optional<List<Facade>> retrieveNavigations() {
		return Optional.of(NavigationCoordinate.navigationConcepts());
	}

	public Optional<Page<Facade>> searchForConceptsWithDescendants(String query, Pageable pageable) {
		return Optional.of(searchService.search(
				query,
				pageable,
				SearchService.SortType.SEMANTIC_SCORE,
				facade -> facade.type() == Type.CONCEPT && !calculatorService.calculateDescendants(facade).isEmpty()));
	}

	public Optional<Page<Facade>> search(String query, Pageable pageable) {
		return Optional.of(searchService.search(
				query,
				pageable,
				SearchService.SortType.SEMANTIC_SCORE));
	}

	public Optional<ScopeNode> retrieveRootScope() {
		Facade rootFacade = new Facade(
				new Id(TinkarTermV2.INTEGRATED_KNOWLEDGE_MANAGEMENT.nid(), TinkarTermV2.INTEGRATED_KNOWLEDGE_MANAGEMENT.publicId().asUuidList().castToList()),
				Type.CONCEPT,
				calculatorService.calculateText(TinkarTermV2.INTEGRATED_KNOWLEDGE_MANAGEMENT.publicId()));
		boolean isLeaf = calculatorService.calculateChildren(rootFacade).isEmpty();
		return Optional.of(new ScopeNode(rootFacade, isLeaf));
	}

	public Optional<ScopeNode> buildScopeNode(Facade facade) {
		boolean isLeaf = calculatorService.calculateChildren(facade).isEmpty();
		return Optional.of(new ScopeNode(facade, isLeaf));
	}

	public Optional<List<ScopeNode>> retrieveChildren(Facade facade) {
		return Optional.of(calculatorService.calculateChildren(facade).stream()
				.map(fac -> new ScopeNode(fac, calculatorService.calculateChildren(fac).isEmpty()))
				.toList());
	}

	public Optional<List<ScopeNode>> retrieveParents(Facade facade) {
		return Optional.of(calculatorService.calculateParents(facade).stream()
				.map(fac -> new ScopeNode(fac, calculatorService.calculateChildren(fac).isEmpty()))
				.toList());
	}

	public Optional<Integer> retrieveDescendantCount(Facade facade) {
		return Optional.of(calculatorService.calculateDescendants(facade).size());
	}

}
