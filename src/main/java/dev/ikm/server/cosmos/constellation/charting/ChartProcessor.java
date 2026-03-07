package dev.ikm.server.cosmos.constellation.charting;

import dev.ikm.server.cosmos.constellation.Step;
import dev.ikm.server.cosmos.ike.Facade;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

	default String findLabel(String nid, Map<Facade, Set<Integer>> scopedConcepts) {
		int conceptNid = Integer.parseInt(nid);
		for (Map.Entry<Facade, Set<Integer>> entry : scopedConcepts.entrySet()) {
			if (entry.getValue().contains(conceptNid)) {
				return entry.getKey().name().replaceAll("[^a-zA-Z0-9]", "");
			}
		}
		return "Concept";
	}

	default boolean isScope(int nid, Set<Facade> scopeFacades) {
		return scopeFacades.stream()
				.anyMatch(scopeFacade -> scopeFacade.id().nid() == nid);
	}
}
