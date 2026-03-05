package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Gatherers;

public class HierarchyChartProcessor implements ChartProcessor {

	private final String hierarchyQuery = """
			UNWIND $batch AS row
			MATCH (child:$(row.childLabel) {id: row.childId, constellationId: row.constellationId})
			MATCH (parent:$(row.parentLabel) {id: row.parentId, constellationId: row.constellationId})
			MERGE (child)-[r:$(row.relLabel) {type: row.relType, constellationId: row.constellationId}]->(parent)""";

	private final String conceptCreateQuery = """
			UNWIND $batch AS row
			MERGE (n:$(row.label) {id: row.id, constellationId: row.constellationId})
			SET n.name = row.name
			""";

	@Override
	public Step getStep() {
		return Step.PROCESS_HIERARCHY;
	}

	@Override
	public String getProcessorName() {
		return "Hierarchy Charting";
	}

	@Override
	public void process(ChartingContext chartingContext, int batchSize) {
		Chart chart = chartingContext.getChart();
		NavigationCalculator navigationCalculator = chartingContext.getChart().navigationCalculator();
		Set<Facade> scopeFacades = chartingContext.getScopedConcepts().keySet();
		List<Map<String, Object>> hierarchyData = new ArrayList<>();
		List<Map<String, Object>> outOfScopeData = new ArrayList<>();

		chartingContext.getScopedConcepts().values()
				.stream()
				.flatMap(List::stream)
				.forEach(childNid -> {
					navigationCalculator.parentsOf(childNid).forEach(parentNid -> {
						if (!isScope(parentNid, scopeFacades)) {
							Map<String, Object> row = new HashMap<>();
							String parentLabel = findLabel(parentNid, chartingContext.getScopedConcepts());
							String childLabel = findLabel(childNid, chartingContext.getScopedConcepts());

							row.put("childId", String.valueOf(childNid));
							row.put("parentId", String.valueOf(parentNid));
							row.put("childLabel", childLabel);
							row.put("parentLabel", parentLabel);
							row.put("constellationId", chart.constellationId().toString());
							row.put("relLabel", "IS_A");
							row.put("relType", "Is-a");

							if (parentLabel.equals("Concept")) {
								Map<String, Object> oosRow = new HashMap<>();
								oosRow.put("id", String.valueOf(parentNid));
								oosRow.put("label", "Concept");
								oosRow.put("name", chart.languageCalculator().getDescriptionTextOrNid(parentNid));
								oosRow.put("constellationId", chart.constellationId().toString());
								outOfScopeData.add(oosRow);
							}
							hierarchyData.add(row);
						}
					});
				});
		writeQueries(conceptCreateQuery, outOfScopeData, chartingContext, batchSize);
		writeQueries(hierarchyQuery, hierarchyData, chartingContext, batchSize);
	}

	private boolean isScope(int nid, Set<Facade> scopeFacades) {
		return scopeFacades.stream()
				.anyMatch(scopeFacade -> scopeFacade.id().nid() == nid);
	}

	private String findLabel(int nid, Map<Facade, List<Integer>> scopedConcepts) {
		for (Map.Entry<Facade, List<Integer>> entry : scopedConcepts.entrySet()) {
			if (entry.getValue().contains(nid)) {
				return entry.getKey().name().replaceAll("[^a-zA-Z0-9]", "");
			}
		}
		return "Concept";
	}

	private void writeQueries(String query, List<Map<String, Object>> data, ChartingContext chartingContext, int batchSize) {
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
