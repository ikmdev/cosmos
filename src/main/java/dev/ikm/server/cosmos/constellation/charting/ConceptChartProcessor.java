package dev.ikm.server.cosmos.constellation.charting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ikm.server.cosmos.constellation.Chart;
import dev.ikm.server.cosmos.constellation.Step;

@Component
public class ConceptChartProcessor extends BaseChartProcessor {

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
				String id = String.valueOf(conceptNid);
				String label = scope.name().replaceAll("[^a-zA-Z0-9]", "");
				String name = chart.languageCalculator().getDescriptionTextOrNid(conceptNid);
				collectRows(id, label, name, constellationId, data);
			}
		});
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
