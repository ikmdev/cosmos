package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ConstellationService {

	private final ConstellationRepository constellationRepository;
	private final CalculatorService calculatorService;
	private final IkeRepository ikeRepository;
	private final Neo4jClient neo4jClient;

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withZone(ZoneId.systemDefault());

	@Autowired
	public ConstellationService(ConstellationRepository constellationRepository, CalculatorService calculatorService, IkeRepository ikeRepository, Neo4jClient neo4jClient) {
		this.constellationRepository = constellationRepository;
		this.calculatorService = calculatorService;
		this.ikeRepository = ikeRepository;
		this.neo4jClient = neo4jClient;
	}

	public Constellation saveNewConstellation(ConstellationForm constellationForm) {
		UUID id = UUID.randomUUID();
		Instant creation = Instant.now();
		//TODO: need to make these more "fetchable", that way it's not a long delay on the UI side
		//	There's a log on startup that the provider must give acccess too
		long concepts = 100;
		long semantics = 200;
		long patterns = 3000;
		long total = concepts + semantics + patterns;

		ConstellationEntity constellationEntity = new ConstellationEntity(
				id,
				constellationForm.name(),
				concepts,
				semantics,
				patterns,
				total,
				0,
				creation,
				null,
				null,
				false
		);
		constellationRepository.createConstellation(constellationEntity);

		return new Constellation(
				id,
				constellationForm.name(),
				formatter.format(Instant.now()),
				0,
				"00:00:00");
	}

	public List<Constellation> retrieveAllConstellations() {
		return constellationRepository.readAll().stream()
				.map(entity ->
						new Constellation(
								entity.id(),
								entity.name(),
								formatter.format(entity.creation()),
								entity.total(),
								"00:00:00"))
				.toList();
	}

	public void removeConstellation(UUID id) {
		constellationRepository.deleteConstellation(id);
	}

	private long countApplicableConcepts() {
		AtomicLong conceptCount = new AtomicLong(0);
		ikeRepository.forEachConcept(nid -> {
			calculatorService.getStampCalculator().latest(nid).ifPresent(_ -> conceptCount.incrementAndGet());
		});
		return conceptCount.get();
	}

	private long countApplicableSemantics() {
		AtomicLong semanticCount = new AtomicLong(0);
		ikeRepository.forEachSemantic(nid -> {
			calculatorService.getStampCalculator().latest(nid).ifPresent(_ -> semanticCount.incrementAndGet());
		});
		return semanticCount.get();
	}

	private long countApplicablePatterns() {
		AtomicLong patternCount = new AtomicLong(0);
		ikeRepository.forEachPattern(nid -> {
			calculatorService.getStampCalculator().latest(nid).ifPresent(_ -> patternCount.incrementAndGet());
		});
		return patternCount.get();
	}

}
