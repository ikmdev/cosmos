package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A dedicated service for running long-running, asynchronous charting tasks.
 */
@Service
public class ChartingService {

	private static final Logger LOG = LoggerFactory.getLogger(ChartingService.class);

	private final ConstellationRepository constellationRepository;
	private final CalculatorService calculatorService;
	private final Neo4jClient neo4jClient;
	private final AtomicBoolean isChartingRunning;
	private final BlockingQueue queue;

	@Autowired
	public ChartingService(ConstellationRepository constellationRepository, CalculatorService calculatorService, Neo4jClient neo4jClient) {
		this.constellationRepository = constellationRepository;
		this.calculatorService = calculatorService;
		this.neo4jClient = neo4jClient;
		this.isChartingRunning = new AtomicBoolean(false);
		this.queue = new ArrayBlockingQueue(10_000);
	}

	/**
	 * Executes the charting process asynchronously.
	 * The @Async annotation tells Spring to run this method on a background thread pool.
	 *
	 * @param constellationId The ID of the constellation to chart.
	 * @return A CompletableFuture holding the result, allowing for further composition if needed.
	 */
	@Async("chartingTaskExecutor")
	public void beginChartingProcess(UUID constellationId) {
		if (!isChartingRunning.compareAndSet(false, true)) {
			throw new IllegalStateException("Another charting is already running.");
		}

		LOG.info("Starting to chart constellation: {}", constellationId);
		try {

			CompletableFuture<Void> neo4jLoad = CompletableFuture.runAsync(() -> {


			});





			neo4jLoad.get();
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			isChartingRunning.set(false);
		}
	}
}