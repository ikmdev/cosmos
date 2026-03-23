
package dev.ikm.server.cosmos.observatory;

import static dev.ikm.server.cosmos.database.CosmosDatabaseConfig.DEFAULT_OBSERVATORY_ID;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

@Service
public class ObservatoryService {

	private final ObservatoryRepository observatoryRepository;
	private final CalculatorService calculatorService;
	private final IkeRepository ikeRepository;
	private final SearchService searchService;

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
				observatoryEntity.excludedModules());
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
				observatory.excludedModules());
	}

	public void bootstrapDefaultObservatory() {
		ObservatoryEntity defaultObservatory = new ObservatoryEntity(
				DEFAULT_OBSERVATORY_ID,
				Instant.now(),
				"Default Observatory",
				StampCoordinate.DEV_LATEST,
				LanguageCoordinate.US_ENG_REG,
				NavigationCoordinate.INFERRED,
				Set.of(),
				Set.of());
		observatoryRepository.createObservatory(defaultObservatory);
	}

	public Observatory saveNewObservatory(ObservatoryForm observatoryForm) {
		UUID id = UUID.randomUUID();
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

}
