package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.server.cosmos.observatory.ObservatoryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

/**
 * A dedicated service for running long-running, asynchronous charting tasks.
 */
@Service
public class ChartingService {

	private static final Logger LOG = LoggerFactory.getLogger(ChartingService.class);

	private final Neo4jClient neo4jClient;
	private final ObservatoryRepository observatoryRepository;
	private final IkeRepository ikeRepository;
	private final BlockingQueue<Chart> chartingQueue;
	private Thread consumerThread;

	private final int batchSize = 5_000;

	@Autowired
	public ChartingService(Neo4jClient neo4jClient, ObservatoryRepository observatoryRepository, IkeRepository ikeRepository) {
		this.neo4jClient = neo4jClient;
		this.observatoryRepository = observatoryRepository;
		this.ikeRepository = ikeRepository;
		this.chartingQueue = new ArrayBlockingQueue<>(100); // Reduced size for local dev
	}

	/**
	 * Starts a background thread that consumes from the charting queue.
	 * This ensures that charting processes are executed sequentially.
	 */
	@PostConstruct
	public void startQueueConsumer() {
		this.consumerThread = Thread.ofVirtual().start(() -> {
			LOG.info("Charting queue consumer started.");
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Chart chart = chartingQueue.take(); // Blocks until an item is available
					LOG.info("Pulled constellation {} from queue for charting.", chart.constellationId());

					// Manually create and configure a new CalculatorService instance for this job.
					CalculatorService calculatorService = new CalculatorService(observatoryRepository, ikeRepository);
					calculatorService.setObservatory(chart.observatoryId());

					performCharting(chart, calculatorService);
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

	@PreDestroy
	public void stopQueueConsumer() {
		if (consumerThread != null && consumerThread.isAlive()) {
			LOG.info("Interrupting charting queue consumer thread for shutdown.");
			consumerThread.interrupt();
		}
	}

	/**
	 * Asynchronously adds a constellation to the charting queue.
	 * The @Async annotation allows the calling thread (e.g., from the controller) to return immediately.
	 *
	 * @param chart The ID of the constellation to chart.
	 */
	@Async("chartingTaskExecutor")
	public void submitChartingJob(Chart chart) {
		try {
			chartingQueue.put(chart);
			LOG.info("Successfully queued constellation {} for charting.", chart.constellationId());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOG.error("Failed to queue constellation {} for charting.", chart.constellationId(), e);
		}
	}

	/**
	 * The actual, synchronous charting logic for a single constellation.
	 * This method is called by the single-threaded queue consumer.
	 */
	private void performCharting(Chart chart, CalculatorService calculatorService) {
		LOG.info("Starting to chart constellation: {}", chart.constellationId());
		List<Integer> conceptsInScope = new ArrayList<>();

		//Extract, Transform, and Load Concept Knowledge into Knowledge Graph
		for (Facade scope : chart.scopes()) {
			List<Integer> conceptNids = extractConcepts(scope, calculatorService);
			if (conceptNids.isEmpty()) {
				throw new IllegalStateException(String.format("No concepts found for scope: %s", scope.name()));
			}
			conceptsInScope.addAll(conceptNids);
		}

		// Now, transform and load the extracted concepts into the knowledge graph.
		transformConcepts(chart.constellationId(), conceptsInScope, getConceptLoadProcess("ChartedConcept"), calculatorService);
		LOG.info("Finished charting constellation: {}", chart.constellationId());
	}

	private List<Integer> extractConcepts(Facade scope, CalculatorService calculatorService) {
		return calculatorService.calculateDescendants(scope).stream().map(facade -> facade.id().nid()).toList();
	}

	private void transformConcepts(UUID constellationId, List<Integer> conceptNids, Consumer<List<Map<String, Object>>> loadProcess, CalculatorService calculatorService) {
		List<Map<String, Object>> batch = new ArrayList<>();
		for (Integer conceptNid : conceptNids) {
			Map<String, Object> row = new HashMap<>();
			row.put("id", conceptNid);
			row.put("name", calculatorService.calculateText(conceptNid));
			row.put("partitionId", constellationId.toString());
			batch.add(row);
			if (batch.size() == batchSize) {
				loadProcess.accept(batch);
				batch.clear();
			}
		}
		if (!batch.isEmpty()) {
			loadProcess.accept(batch);
		}
	}

	private Consumer<List<Map<String, Object>>> getConceptLoadProcess(String nodeLabel) {
		return batch -> {
			String cypher = String.format("""
					UNWIND $batch AS row
					MERGE (n:%s {id: row.id, partitionId: row.partitionId})
					SET n.name = row.name
					""", nodeLabel);
			neo4jClient.query(cypher).bind(batch).to("batch").run();
		};
	}
}