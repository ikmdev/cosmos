package dev.ikm.server.cosmos.constellation;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.constellation.charting.ChartingService;
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.Id;
import dev.ikm.server.cosmos.ike.Type;
import dev.ikm.server.cosmos.observatory.Observatory;
import dev.ikm.server.cosmos.observatory.ObservatoryService;
import dev.ikm.server.cosmos.observatory.ScopeNode;
import dev.ikm.server.cosmos.search.SearchService;
import dev.ikm.tinkar.terms.TinkarTermV2;

@Service
public class ConstellationService {

	private final ConstellationRepository constellationRepository;
	private final ChartingService chartingService;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withZone(ZoneId.systemDefault());
	private final CalculatorService calculatorService;
	private final ObservatoryService observatoryService;
	private final SearchService searchService;

	
	public ConstellationService(ConstellationRepository constellationRepository, ChartingService chartingService,
			CalculatorService calculatorService,
			ObservatoryService observatoryService, SearchService searchService) {
		this.constellationRepository = constellationRepository;
		this.chartingService = chartingService;
		this.calculatorService = calculatorService;
		this.observatoryService = observatoryService;
		this.searchService = searchService;
	}

	public Optional<Constellation> createConstellation(UUID observatoryId, ConstellationForm constellationForm) {
		UUID id = UUID.randomUUID();
		Instant created = Instant.now();

		ConstellationEntity constellationEntity = new ConstellationEntity(
				id,
				observatoryId,
				Phase.QUEUED,
				Step.PROCESS_CONCEPTS,
				constellationForm.name(),
				constellationForm.selectedIncludedScopes(),
				constellationForm.portalPrompt(),
				0,
				0,
				0,
				created,
				null);
		constellationRepository.createConstellation(constellationEntity);

		// Start the charting process
		startCharting(id);

		return Optional.of(new Constellation(
				id,
				observatoryId,
				constellationEntity.phase().display(),
				constellationEntity.step().getDisplay(),
				constellationForm.name(),
				constellationForm.selectedIncludedScopes(),
				constellationForm.portalPrompt(),
				formatter.format(created),
				0,
				formatDuration(constellationEntity.getDuration()),
				constellationEntity.isCompleted()));
	}

	public Optional<List<Constellation>> retrieveAllConstellations() {
		// Delegate to the specific method with a null ID to fetch all constellations.
		return retrieveAllConstellations(null);
	}

	public Optional<List<Constellation>> retrieveAllConstellations(UUID observatoryId) {
		List<ConstellationEntity> entities;
		if (observatoryId == null) {
			entities = constellationRepository.readAll();
		} else {
			// This assumes you will add a `readAllByObservatory(UUID observatoryId)` method
			// to your repository to filter at the database level.
			entities = constellationRepository.readAll(observatoryId);
		}
		return Optional.of(entities.stream().map(this::mapEntityToDto).toList());
	}

	public Optional<Constellation> retrieveConstellation(UUID id) {
		ConstellationEntity constellationEntity = constellationRepository.readConstellation(id);
		return Optional.of(mapEntityToDto(constellationEntity));
	}

	public void removeConstellation(UUID id) {
		chartingService
				.submitChartingJob(new Chart(Action.DELETE, id, null, Set.of(), Set.of(), Set.of(), null, null, null));
		constellationRepository.deleteConstellation(id);
	}

	public Optional<Constellation> getConstellationStatus(UUID id) {
		ConstellationEntity constellationEntity = constellationRepository.readConstellation(id);
		return Optional.of(mapEntityToDto(constellationEntity));
	}

	private void startCharting(UUID constellationId) {
		// Synchronously update the phase to give the user immediate feedback.
		constellationRepository.updatePhase(constellationId, Phase.QUEUED);
		UUID observatoryId = calculatorService.getObservatoryId();
		Observatory observatory = observatoryService.retrieveObservatory(observatoryId).orElseThrow();
		Chart chart = new Chart(Action.CREATE, constellationId, observatoryId, Set.of(), //TODO - fix me
				observatory.includedModules(), observatory.excludedModules(),
				calculatorService.getStampCalculator(), calculatorService.getLanguageCalculator(),
				calculatorService.getNavigationCalculator());
		chartingService.submitChartingJob(chart);
	}

	public void changeConstellationPhase(UUID id, Phase phase) {
		constellationRepository.updatePhase(id, phase);
	}

	public void addToConceptCount(UUID id, int count) {
		constellationRepository.updateConceptCount(id, count);
	}

	private String formatDuration(Duration duration) {
		if (duration == null) {
			return "00:00:00";
		}
		long hours = duration.toHours();
		int minutes = duration.toMinutesPart();
		int seconds = duration.toSecondsPart();
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	private Constellation mapEntityToDto(ConstellationEntity entity) {
		return new Constellation(
				entity.id(),
				entity.observatoryId(),
				entity.phase().display(),
				entity.step().getDisplay(),
				entity.name(),
				entity.scopes(),
				entity.portalPrompt(),
				formatter.format(entity.created()),
				entity.total(),
				formatDuration(entity.getDuration()),
				entity.isCompleted());
	}

	public Optional<Page<Facade>> searchForConceptsWithDescendants(String query, Pageable pageable) {
		return Optional.of(searchService.search(
				query,
				pageable,
				SearchService.SortType.SEMANTIC_SCORE,
				facade -> facade.type() == Type.CONCEPT && !calculatorService.calculateDescendants(facade).isEmpty()));
	}

	public Optional<org.springframework.data.domain.Page<Facade>> search(String query, org.springframework.data.domain.Pageable pageable) {
		return Optional.of(searchService.search(
				query,
				pageable,
				SearchService.SortType.SEMANTIC_SCORE));
	}

	public Optional<ScopeNode> retrieveRootScope() {
		Facade rootFacade = new Facade(
				new Id(TinkarTermV2.INTEGRATED_KNOWLEDGE_MANAGEMENT.nid(),
						TinkarTermV2.INTEGRATED_KNOWLEDGE_MANAGEMENT.publicId().asUuidList().castToList()),
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
