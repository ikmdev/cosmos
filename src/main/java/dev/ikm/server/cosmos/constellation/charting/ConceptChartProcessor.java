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
		List<Map<String, Object>> data = new ArrayList<>();

		chartContext.getScopedConcepts().forEach((scope, descendants) -> {
			String scopeId = String.valueOf(scope.id().nid());
			String scopeLabel = findLabel(scopeId, chartContext.getScopedConcepts());
			String scopeName = chart.languageCalculator().getDescriptionTextOrNid(scope.id().nid());
			String constellationId = chart.constellationId().toString();
			collectRows(scopeId, scopeLabel, scopeName, constellationId, data);

			for (Integer conceptNid : descendants) {
				Map<String, Object> row = new HashMap<>();
				String id = String.valueOf(conceptNid);
				String label = scope.name().replaceAll("[^a-zA-Z0-9]", "");
				String name = chart.languageCalculator().getDescriptionTextOrNid(conceptNid);
				collectRows(id, label, name, constellationId, data);
			}
		});

		writeData(cypherQuery, data, chartContext, batchSize);
	}

	private void collectRows(String id, String label, String name, String constellationId, List<Map<String, Object>> data) {
		Map<String, Object> row = new HashMap<>();
		row.put("id", id);
		row.put("label", label);
		row.put("name", name);
		row.put("constellationId", constellationId);
		data.add(row);
	}
}
