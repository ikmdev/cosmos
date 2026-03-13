package dev.ikm.server.cosmos.constellation.charting;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.IkeRepository;
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
	private final ObservatoryRepository observatoryRepository;
	private final IkeRepository ikeRepository;
	private final BlockingQueue<Chart> chartingQueue;
	private final List<ChartProcessor> chartProcessors;

	private Thread consumerThread;

	public ChartingService(ConstellationRepository constellationRepository,
						   ObservatoryRepository observatoryRepository,
						   IkeRepository ikeRepository,
						   List<ChartProcessor> chartProcessors) {
		this.observatoryRepository = observatoryRepository;
		this.constellationRepository = constellationRepository;
		this.ikeRepository = ikeRepository;
		this.chartingQueue = new ArrayBlockingQueue<>(100); // Reduced size for local dev
		this.chartProcessors = chartProcessors;
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
					} else if (chart.action() == Action.CREATE) {
						constellationRepository.updatePhase(chart.constellationId(), Phase.CHARTING);
						performCharting(chart);
					}

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

	private void performChartDelete(Chart chart) {
		
	}

	/**
	 * The actual, synchronous charting logic for a single constellation.
	 * This method is called by the single-threaded queue consumer.
	 */
	private void performCharting(Chart chart) {
		LOG.info("Starting to chart constellation: {}", chart.constellationId());

		//Map Facades to their descendants or concepts within "scope" of the knowledge graph
		Map<Facade, Set<Integer>> scopedConcepts = extractConcepts(chart, chart.scopes());

		//Create ChartContext and fire off ChartProcessors
		ChartingContext chartContext = new ChartingContext(
				chart,
				scopedConcepts,
				progressUpdate -> {
					switch (progressUpdate.step()) {
						case PROCESS_CONCEPTS -> constellationRepository.updateConceptCount(chart.constellationId(), progressUpdate.processedCount());
						case PROCESS_HIERARCHY, PROCESS_SEMANTICS, PROCESS_LOGICAL_DEFINITIONS ->
								constellationRepository.updateSemanticCount(chart.constellationId(), progressUpdate.processedCount());
					}
				});

		//Apply ChartContext to each ChartProcessor
		for (ChartProcessor processor : chartProcessors) {
			processor.process(chartContext, 5_000);
		}

		constellationRepository.updateCompleted(chart.constellationId(), Instant.now());
		constellationRepository.updatePhase(chart.constellationId(), Phase.CHARTED);
		LOG.info("Finished charting constellation: {}", chart.constellationId());
	}

	private Map<Facade, Set<Integer>> extractConcepts(Chart chart, Set<Facade> scopes) {
		Map<Facade, Set<Integer>> scopedConcepts = new HashMap<>();
		for (Facade scope : scopes) {
			scopedConcepts.put(scope, chart.navigationCalculator().descendentsOf(scope.id().nid()).mapToSet(nid -> nid));
		}
		return scopedConcepts;
	}

}