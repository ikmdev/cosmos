package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
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
	private final CalculatorService calculatorService;
	private final IkeRepository ikeRepository;
	private final Neo4jClient neo4jClient;


	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withZone(ZoneId.systemDefault());

	@Autowired
	public ConstellationService(ConstellationRepository constellationRepository, ChartingService chartingService, CalculatorService calculatorService, IkeRepository ikeRepository, Neo4jClient neo4jClient) {
		this.constellationRepository = constellationRepository;
		this.chartingService = chartingService;
		this.calculatorService = calculatorService;
		this.ikeRepository = ikeRepository;
		this.neo4jClient = neo4jClient;
	}

	public Constellation saveNewConstellation(ConstellationForm constellationForm) {
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
		// 1. Immediately update the phase to CHARTING so the UI reflects the change.
		constellationRepository.updatePhase(id, Phase.CHARTING);

		// 2. Publish the task to the asynchronous ChartingService.
		// This call returns immediately.
		chartingService.beginChartingProcess(id);

		// 3. Return the current status. The web request completes instantly,
		// while the task runs in the background.
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
