package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A dedicated service for running long-running, asynchronous charting tasks.
 */
@Service
public class ChartingService {

	private static final Logger LOG = LoggerFactory.getLogger(ChartingService.class);

	private final ConstellationRepository constellationRepository;
	private final CalculatorService calculatorService;
	private final Neo4jClient neo4jClient;
	private final BlockingQueue<UUID> chartingQueue;

	@Autowired
	public ChartingService(ConstellationRepository constellationRepository, CalculatorService calculatorService, Neo4jClient neo4jClient) {
		this.constellationRepository = constellationRepository;
		this.calculatorService = calculatorService;
		this.neo4jClient = neo4jClient;
		this.chartingQueue = new ArrayBlockingQueue<>(10_000);
	}

	/**
	 * Starts a background thread that consumes from the charting queue.
	 * This ensures that charting processes are executed sequentially.
	 */
	@PostConstruct
	public void startQueueConsumer() {
		Thread.ofVirtual().start(() -> {
			LOG.info("Charting queue consumer started.");
			while (!Thread.currentThread().isInterrupted()) {
				try {
					UUID constellationId = chartingQueue.take(); // Blocks until an item is available
					LOG.info("Pulled constellation {} from queue for charting.", constellationId);
					constellationRepository.updatePhase(constellationId, Phase.CHARTING);
					performCharting(constellationId);
				} catch (InterruptedException e) {
					LOG.warn("Charting queue consumer was interrupted.", e);
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					// Catching exceptions here ensures the consumer loop doesn't die if one charting process fails.
					LOG.error("Unhandled exception during charting process. Continuing to next item.", e);
				}
			}
			LOG.info("Charting queue consumer stopped.");
		});
	}

	/**
	 * Asynchronously adds a constellation to the charting queue.
	 * The @Async annotation allows the calling thread (e.g., from the controller) to return immediately.
	 *
	 * @param constellationId The ID of the constellation to chart.
	 */
	@Async("chartingTaskExecutor")
	public void submitChartingJob(UUID constellationId) {
		try {
			chartingQueue.put(constellationId);
			LOG.info("Successfully queued constellation {} for charting.", constellationId);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOG.error("Failed to queue constellation {} for charting.", constellationId, e);
		}
	}

	/**
	 * The actual, synchronous charting logic for a single constellation.
	 * This method is called by the single-threaded queue consumer.
	 */
	private void performCharting(UUID constellationId) {
		LOG.info("Starting to chart constellation: {}", constellationId);

		try {

			for (int i = 0; i < 10; i++) {
				Thread.sleep(1_000);
				constellationRepository.updateConceptCount(constellationId, i+2);
				constellationRepository.updateSemanticCount(constellationId, i *10);
				constellationRepository.updatePatternCount(constellationId, i +1);
			}


		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		constellationRepository.updatePhase(constellationId, Phase.CHARTED);
		constellationRepository.updateCompleted(constellationId, Instant.now());

//		// Here, you would update the constellation's status to "CHARTING".
//
//		// 1. Create indices to ensure fast lookups by constellationId.
//		neo4jClient.query("CREATE INDEX constellation_id_index IF NOT EXISTS FOR (n:Constellation) ON (n.id)").run();
//		neo4jClient.query("CREATE INDEX star_constellation_id_index IF NOT EXISTS FOR (n:Star) ON (n.constellationId)").run();
//
//		// 2. Example: Create a root node for this constellation.
//		neo4jClient.query("""
//			MERGE (c:Constellation {id: $constellationId})
//			ON CREATE SET c.name = 'Constellation ' + $constellationId, c.createdAt = timestamp()
//			""").bind(constellationId.toString()).to("constellationId").run();
//
//		// 3. Example: Create a new 'Star' node and link it to its constellation.
//		neo4jClient.query("""
//			MATCH (c:Constellation {id: $constellationId})
//			CREATE (s:Star {name: 'New Star', constellationId: $constellationId})
//			CREATE (c)-[:CONTAINS]->(s)
//			""").bind(constellationId.toString()).to("constellationId").run();
//
		LOG.info("Finished charting constellation: {}", constellationId);
//		// Here, you would update the constellation's status to "COMPLETED".
	}
}