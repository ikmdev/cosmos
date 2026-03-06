package dev.ikm.server.cosmos.constellation.charting;

import dev.ikm.server.cosmos.constellation.Step;

import java.util.List;
import java.util.Map;
import java.util.stream.Gatherers;

public interface ChartProcessor {

	Step getStep();

	String getProcessorName();

	void process(ChartingContext chartContext, int batchSize);

	default void writeData(String query, List<Map<String, Object>> data, ChartingContext chartingContext, int batchSize) {
		data.stream()
				.gather(Gatherers.windowFixed(batchSize))
				.forEach(batch -> {
					chartingContext.getNeo4jClient().query(query)
							.bind(batch)
							.to("batch")
							.run();
					chartingContext.reportProgress(getStep(), batch.size());
				});
	}
}
