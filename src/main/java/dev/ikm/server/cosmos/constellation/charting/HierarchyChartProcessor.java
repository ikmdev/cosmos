package dev.ikm.server.cosmos.constellation.charting;

import dev.ikm.server.cosmos.constellation.Chart;
import dev.ikm.server.cosmos.constellation.Step;
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class HierarchyChartProcessor extends BaseChartProcessor {

	private final String hierarchyQuery = """
			UNWIND $batch AS row
			MATCH (child:$(row.childLabel) {id: row.childId, constellationId: row.constellationId})
			MATCH (parent:$(row.parentLabel) {id: row.parentId, constellationId: row.constellationId})
			MERGE (child)-[r:$(row.relLabel) {type: row.relType, constellationId: row.constellationId}]->(parent)""";

	private final String outOfScopeCreateConceptQuery = """
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

		chartingContext.getScopedConcepts().forEach((facade, descendants) -> {
			descendants.forEach(childNid -> {
				navigationCalculator.parentsOf(childNid).forEach(parentNid -> {
					String childId = String.valueOf(childNid);
					String parentId = String.valueOf(parentNid);
					String parentLabel = findLabel(String.valueOf(parentNid), chartingContext.getScopedConcepts());
					String childLabel = findLabel(String.valueOf(childNid), chartingContext.getScopedConcepts());
					String constellationId = chart.constellationId().toString();
					collectHierarchyRows(childId, parentId, childLabel, parentLabel, constellationId, hierarchyData);

					if (parentLabel.equals("Concept")) {
						String conceptId = String.valueOf(parentNid);
						String name = chart.languageCalculator().getDescriptionTextOrNid(parentNid);
						collectOutOfScopeConceptRows(conceptId, "Concept", name, constellationId, outOfScopeData);
					}
				});
			});
		});

		writeNodeData(outOfScopeCreateConceptQuery, outOfScopeData, chartingContext, batchSize, true);
		writeRelationshipData(hierarchyQuery, hierarchyData, chartingContext, batchSize);
	}

	private void collectHierarchyRows(String childId, String parentId, String childLabel, String parentLabel, String constellationId, List<Map<String, Object>> data) {
		Map<String, Object> row = new HashMap<>();
		row.put("childId", childId);
		row.put("parentId", parentId);
		row.put("childLabel", childLabel);
		row.put("parentLabel", parentLabel);
		row.put("constellationId", constellationId);
		row.put("relLabel", "IS_A");
		row.put("relType", "Is-a");
		data.add(row);
	}

	private void collectOutOfScopeConceptRows(String id, String label, String name, String constellationId, List<Map<String, Object>> data) {
		Map<String, Object> row = new HashMap<>();
		row.put("id", id);
		row.put("label", label);
		row.put("name", name);
		row.put("constellationId", constellationId);
		data.add(row);
	}
}
