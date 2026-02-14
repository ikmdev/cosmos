package dev.ikm.server.cosmos.constellation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

	public Constellation formConstellation(ConstellationForm constellationForm) {
		UUID id = UUID.randomUUID();
		Instant created = Instant.now();

		ConstellationEntity constellationEntity = new ConstellationEntity(
				id,
				Phase.FORMING,
				constellationForm.name(),
				0,
				0,
				0,
				created,
				null);
		constellationRepository.createConstellation(constellationEntity);

		return new Constellation(
				id,
				constellationEntity.phase().display(),
				constellationForm.name(),
				formatter.format(created),
				0,
				formatDuration(constellationEntity.getDuration()),
				constellationEntity.isCompleted());
	}

	public List<Constellation> retrieveAllConstellations() {
		return constellationRepository.readAll().stream()
				.map(entity ->
						new Constellation(
								entity.id(),
								entity.phase().display(),
								entity.name(),
								formatter.format(entity.created()),
								entity.total(),
								formatDuration(entity.getDuration()),
								entity.isCompleted()))
				.toList();
	}

	public void removeConstellation(UUID id) {
		constellationRepository.deleteConstellation(id);
	}

	public Constellation getConstellationStatus(UUID id) {
		ConstellationEntity constellationEntity = constellationRepository.readConstellation(id);
		Duration processDuration = constellationEntity.getDuration();
		return new Constellation(
				id,
				constellationEntity.phase().display(),
				constellationEntity.name(),
				formatter.format(constellationEntity.created()),
				constellationEntity.total(),
				formatDuration(processDuration),
				constellationEntity.isCompleted());
	}

	public Constellation startCharting(UUID id) {
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
