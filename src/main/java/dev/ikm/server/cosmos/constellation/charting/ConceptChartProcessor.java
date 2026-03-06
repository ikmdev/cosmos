package dev.ikm.server.cosmos.constellation.charting;

import dev.ikm.server.cosmos.constellation.Chart;
import dev.ikm.server.cosmos.constellation.Step;
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
		List<Map<String, Object>> data = new ArrayList<>();

		chartContext.getScopedConcepts().forEach((scope, descendants) -> {
			for (Integer conceptNid : descendants) {
				Map<String, Object> row = new HashMap<>();
				row.put("id", String.valueOf(conceptNid));
				row.put("label", scope.name().replaceAll("[^a-zA-Z0-9]", ""));
				row.put("name", chart.languageCalculator().getDescriptionTextOrNid(conceptNid));
				row.put("constellationId", chart.constellationId().toString());
				data.add(row);

			}
		});

		writeData(cypherQuery, data, chartContext, batchSize);
	}
}
