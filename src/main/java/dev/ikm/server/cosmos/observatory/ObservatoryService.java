
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
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.entity.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
				observatoryEntity.includedModules().stream()
						.map(uuids -> new Facade(new Id(Entity.nid(PublicIds.of(uuids)), uuids), Type.CONCEPT, calculatorService.calculateText(PublicIds.of(uuids))))
						.toList(),
				observatoryEntity.excludedModules().stream()
						.map(uuids -> new Facade(new Id(Entity.nid(PublicIds.of(uuids)), uuids), Type.CONCEPT, calculatorService.calculateText(PublicIds.of(uuids))))
						.toList());
	}

	private ObservatoryEntity buildObservatoryEntity(Observatory observatory) {
		StampCoordinate stampCoordinate = StampCoordinate.fromId(observatory.stampCoordinate().id());
		LanguageCoordinate languageCoordinate = LanguageCoordinate.fromId(observatory.languageCoordinate().id());
		NavigationCoordinate navigationCoordinate = NavigationCoordinate.fromId(observatory.navigationCoordinate().id());
		List<List<UUID>> includedModules = observatory.includedModules().stream()
				.map(component -> PublicIds.of(component.id().uuids()))
				.map(publicId -> publicId.asUuidList().castToList())
				.toList();
		List<List<UUID>> excludedModules = observatory.excludedModules().stream()
				.map(component -> PublicIds.of(component.id().uuids()))
				.map(publicId -> publicId.asUuidList().castToList())
				.toList();
		return new ObservatoryEntity(observatory.id(), Instant.now(), observatory.name(), stampCoordinate, languageCoordinate, navigationCoordinate, includedModules, excludedModules);
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
				observatoryForm.selectedExcludedModules());
		ObservatoryEntity entity = buildObservatoryEntity(observatory);
		observatoryRepository.createObservatory(entity);
		return observatory;
	}

	public Observatory retrieveObservatory(UUID id) {
		ObservatoryEntity observatoryEntity = observatoryRepository.readObservatory(id);
		return buildObservatory(observatoryEntity);
	}

	public List<Observatory> retrieveAllObservatories() {
		return observatoryRepository.readAll().stream()
				.sorted(Comparator.comparing(ObservatoryEntity::modified).reversed())
				.map(this::buildObservatory)
				.toList();
	}

	public void removeObservatory(UUID id) {
		observatoryRepository.deleteObservatory(id);
	}

	public void updateObservatory(Observatory observatory) {
		ObservatoryEntity entity = buildObservatoryEntity(observatory);
		observatoryRepository.updateObservatory(observatory.id(), entity);
	}

	public List<Facade> retrieveModules() {
		List<PublicId> publicIds = ikeRepository.findAllModules();
		return publicIds.stream()
				.map(publicId -> new Facade(new Id(Entity.nid(publicId), Arrays.asList(publicId.asUuidArray())), Type.CONCEPT, calculatorService.calculateText(publicId)))
				.toList();
	}

	public List<Facade> retrieveStamps() {
		return StampCoordinate.stampConcepts();
	}

	public List<Facade> retrieveLanguages() {
		return LanguageCoordinate.languageConcepts();
	}

	public List<Facade> retrieveNavigations() {
		return NavigationCoordinate.navigationConcepts();
	}

	public Page<Facade> searchForConceptsWithDescendants(String query, Pageable pageable) {
		return searchService.search(
				query,
				pageable,
				SearchService.SortType.SEMANTIC_SCORE,
				facade -> facade.type() == Type.CONCEPT && !calculatorService.calculateDescendants(facade).isEmpty());
	}
}
