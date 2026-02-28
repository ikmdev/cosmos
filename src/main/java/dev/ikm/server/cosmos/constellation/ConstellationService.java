package dev.ikm.server.cosmos.constellation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
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
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withZone(ZoneId.systemDefault());

	@Autowired
	public ConstellationService(ConstellationRepository constellationRepository, ChartingService chartingService) {
		this.constellationRepository = constellationRepository;
		this.chartingService = chartingService;
	}

	public Optional<Constellation> formConstellation(UUID observatoryId, ConstellationForm constellationForm) {
		UUID id = UUID.randomUUID();
		Instant created = Instant.now();

		ConstellationEntity constellationEntity = new ConstellationEntity(
				id,
				observatoryId,
				Phase.FORMING,
				constellationForm.name(),
				0,
				0,
				0,
				created,
				null);
		constellationRepository.createConstellation(constellationEntity);

		return Optional.of(new Constellation(
				id,
				observatoryId,
				constellationEntity.phase().display(),
				constellationForm.name(),
				formatter.format(created),
				0,
				formatDuration(constellationEntity.getDuration()),
				constellationEntity.isCompleted()));
	}

	public Optional<List<Constellation>> retrieveAllConstellations() {
		return Optional.of(constellationRepository.readAll().stream()
				.map(entity ->
						new Constellation(
								entity.id(),
								entity.observatoryId(),
								entity.phase().display(),
								entity.name(),
								formatter.format(entity.created()),
								entity.total(),
								formatDuration(entity.getDuration()),
								entity.isCompleted()))
				.toList());
	}

	public Optional<List<Constellation>> retrieveAllConstellations(UUID observatoryId) {
		Optional<List<Constellation>> optionalConstellations = retrieveAllConstellations();
		return optionalConstellations.map(constellations -> constellations.stream()
				.filter(constellation -> constellation.observatoryId().equals(observatoryId))
				.toList());
	}

	public void removeConstellation(UUID id) {
		constellationRepository.deleteConstellation(id);
	}

	public Optional<Constellation> getConstellationStatus(UUID id) {
		ConstellationEntity constellationEntity = constellationRepository.readConstellation(id);
		Duration processDuration = constellationEntity.getDuration();
		return Optional.of(new Constellation(
				id,
				constellationEntity.observatoryId(),
				constellationEntity.phase().display(),
				constellationEntity.name(),
				formatter.format(constellationEntity.created()),
				constellationEntity.total(),
				formatDuration(processDuration),
				constellationEntity.isCompleted()));
	}

	public Optional<Constellation> startCharting(UUID id) {
		// Synchronously update the phase to give the user immediate feedback.
		constellationRepository.updatePhase(id, Phase.QUEUED);
		chartingService.submitChartingJob(id);
		return getConstellationStatus(id);
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

}
