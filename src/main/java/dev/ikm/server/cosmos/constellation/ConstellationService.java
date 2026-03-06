package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.constellation.charting.ChartingService;
import dev.ikm.server.cosmos.observatory.Observatory;
import dev.ikm.server.cosmos.observatory.ObservatoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConstellationService {

	private final ConstellationRepository constellationRepository;
	private final ChartingService chartingService;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
	private final CalculatorService calculatorService;
	private final ObservatoryService observatoryService;

	@Autowired
	public ConstellationService(ConstellationRepository constellationRepository, ChartingService chartingService, CalculatorService calculatorService, ObservatoryService observatoryService) {
		this.constellationRepository = constellationRepository;
		this.chartingService = chartingService;
		this.calculatorService = calculatorService;
		this.observatoryService = observatoryService;
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
				0,
				0,
				0,
				created,
				null);
		constellationRepository.createConstellation(constellationEntity);

		//Start the charting process
		startCharting(id);

		return Optional.of(new Constellation(
				id,
				observatoryId,
				constellationEntity.phase().display(),
				constellationEntity.step().getDisplay(),
				constellationForm.name(),
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
		chartingService.submitChartingJob(new Chart(Action.DELETE, id, null, List.of(), null, null, null));
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
		Chart chart = new Chart(Action.CREATE, constellationId, observatoryId, observatory.includedScopes(),
				calculatorService.getStampCalculator(), calculatorService.getLanguageCalculator(), calculatorService.getNavigationCalculator());
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
				formatter.format(entity.created()),
				entity.total(),
				formatDuration(entity.getDuration()),
				entity.isCompleted());
	}

}
