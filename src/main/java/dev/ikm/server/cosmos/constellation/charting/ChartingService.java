package dev.ikm.server.cosmos.constellation.charting;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import dev.ikm.server.cosmos.constellation.Action;
import dev.ikm.server.cosmos.constellation.Chart;
import dev.ikm.server.cosmos.constellation.ConstellationRepository;
import dev.ikm.server.cosmos.constellation.Phase;
import dev.ikm.server.cosmos.observatory.ObservatoryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * A dedicated service for running long-running, asynchronous charting tasks.
 */
@Service
public class ChartingService {

	private static final Logger LOG = LoggerFactory.getLogger(ChartingService.class);

	private final ConstellationRepository constellationRepository;
	private final BlockingQueue<Chart> chartingQueue;
	private final EmbeddingChartProcessor embeddingChartProcessor;
	private final DeleteChartProcessor deleteChartProcessor;
	private final int batchSize = 100; // This can be made configurable as needed

	private Thread consumerThread;

	public ChartingService(ConstellationRepository constellationRepository,
			ObservatoryRepository observatoryRepository,
			EmbeddingChartProcessor embeddingChartProcessor,
			DeleteChartProcessor deleteChartProcessor) {
		this.constellationRepository = constellationRepository;
		this.chartingQueue = new ArrayBlockingQueue<>(100); // Reduced size for local dev
		this.embeddingChartProcessor = embeddingChartProcessor;
		this.deleteChartProcessor = deleteChartProcessor;
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

					if (chart.action() == Action.DELETE) {
						performChartDelete(chart);
					} else {
						performCharting(chart);
					}
				} catch (InterruptedException e) {
					LOG.warn("Charting queue consumer was interrupted.", e);
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					// Catching exceptions here ensures the consumer loop doesn't die if one
					// charting process fails.
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
	 * The @Async annotation allows the calling thread (e.g., from the controller)
	 * to return immediately.
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
	private void performCharting(Chart chart) {
		LOG.info("Starting to chart constellation: {}", chart.constellationId());

		ChartingContext chartingContext = new ChartingContext(chart, progress -> {
			constellationRepository.updateProgress(chart.constellationId(), progress);
			LOG.info("Charting progress for constellation {}: {}%", chart.constellationId(), progress);
		}, batchSize); // Batch size can be adjusted as needed

		constellationRepository.updatePhase(chart.constellationId(), Phase.CHARTING);
		embeddingChartProcessor.process(chartingContext);

		constellationRepository.updateCompleted(chart.constellationId(), Instant.now());
		constellationRepository.updatePhase(chart.constellationId(), Phase.CHARTED);
		LOG.info("Finished charting constellation: {}", chart.constellationId());
	}

	private void performChartDelete(Chart chart) {
		LOG.info("Starting to delete charts for constellation: {}", chart.constellationId());

		deleteChartProcessor.process(new ChartingContext(chart, progress -> {
		}, batchSize));

		LOG.info("Finished deleting charts for constellation: {}", chart.constellationId());
	}
}