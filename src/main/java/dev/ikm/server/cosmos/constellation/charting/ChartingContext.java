package dev.ikm.server.cosmos.constellation.charting;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import dev.ikm.server.cosmos.constellation.Chart;
import dev.ikm.server.cosmos.constellation.Step;
import dev.ikm.server.cosmos.ike.Facade;

public class ChartingContext {

	public record ProgressUpdate(Step step, long processedCount) {}

	private final Chart chart;
	private final Map<Facade, Set<Integer>> scopedConcepts;
	private final Consumer<ProgressUpdate> progressConsumer;
	private long processCount;

	public ChartingContext(Chart chart, Map<Facade, Set<Integer>> scopedConcepts, Consumer<ProgressUpdate> progressConsumer) {
		this.chart = chart;
		this.scopedConcepts = scopedConcepts;
		this.progressConsumer = progressConsumer;
		this.processCount = 0;
	}

	public Chart getChart() {
		return chart;
	}

	public Map<Facade, Set<Integer>> getScopedConcepts() {
		return scopedConcepts;
	}


	public void reportProgress(Step step, long processedCount) {
		if (this.progressConsumer != null) {
			this.processCount += processedCount;
			this.progressConsumer.accept(new ProgressUpdate(step, processCount));
		}
	}
}