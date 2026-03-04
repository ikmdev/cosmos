package dev.ikm.server.cosmos.constellation;

import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConceptChartProcessor implements ChartProcessor {

	private final String cypherQuery = """
						UNWIND $batch AS row
						MERGE (n:$(row.label) {id: row.id, constellationId: row.constellationId})
						SET n.name = row.name
						""";

	@Override
	public Step getStep() {
		return Step.PROCESS_CONCEPTS;
	}

	@Override
	public String getProcessorName() {
		return "Concept Charting";
	}

	@Override
	public void process(ChartingContext chartContext, int batchSize) {
		Chart chart = chartContext.getChart();
		Neo4jClient neo4jClient = chartContext.getNeo4jClient();
		chartContext.getScopedConcepts().forEach((scope, descendants) -> {
			List<Map<String, Object>> batch = new ArrayList<>();
			for (Integer conceptNid : descendants) {
				Map<String, Object> row = new HashMap<>();
				row.put("id", String.valueOf(conceptNid));
				row.put("label", scope.name().replaceAll("[^a-zA-Z0-9]", ""));
				row.put("name", chart.languageCalculator().getDescriptionTextOrNid(conceptNid));
				row.put("constellationId", chart.constellationId().toString());
				batch.add(row);
				if (batch.size() == batchSize) {
					neo4jClient.query(cypherQuery)
							.bind(batch)
							.to("batch")
							.run();
					chartContext.reportProgress(getStep(), batch.size());
					batch.clear();
				}
			}
			if (!batch.isEmpty()) {
				neo4jClient.query(cypherQuery)
						.bind(batch)
						.to("batch")
						.run();
				chartContext.reportProgress(getStep(), batch.size());
			}
		});
	}
}
