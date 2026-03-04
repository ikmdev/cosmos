package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HierarchyChartProcessor implements ChartProcessor {

	private final String cypherQuery = """
		  UNWIND $batch AS row
		  MATCH (child:$(row.childLabel) {id: row.childId, constellationId: row.constellationId})

		  OPTIONAL MATCH (parent:$(row.parentLabel) {id: row.parentId, constellationId: row.constellationId})

		  // Find or create the generic node
		  MERGE (generic:Concept {id: "GENERIC_FALLBACK", constellationId: row.constellationId})
			ON CREATE SET generic.name = coalesce(row.conceptName, "Default Concept Name")

		  WITH child, row, coalesce(parent, generic) AS targetNode

		  MERGE (child)-[r:$(row.relLabel) {type: row.relType, constellationId: row.constellationId}]->(targetNode)
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
	public void process(ChartingContext chartContext, int batchSize) {
		Chart chart = chartContext.getChart();
		Neo4jClient neo4jClient = chartContext.getNeo4jClient();
		NavigationCalculator navigationCalculator = chartContext.getChart().navigationCalculator();
		Set<Facade> scopeFacades = chartContext.getScopedConcepts().keySet();
		/*
			Need to be able to handle a single parent and multiple parents. Multiple parents may result
				A) Both in scope
				B) Both out of scope
				C) Some in and some out

			If not in-scope, we should return a label of "Concept" to not lose representation and create a node for it
		 */
		chartContext.getScopedConcepts().values()
				.stream()
				.flatMap(List::stream)
				.forEach(childNid -> {
					List<Map<String, Object>> batch = new ArrayList<>();
					navigationCalculator.parentsOf(childNid).forEach(parentNid -> {

						if (!isScope(parentNid, scopeFacades)) {
							Map<String, Object> row = new HashMap<>();
							String parentLabel = findLabel(parentNid, chartContext.getScopedConcepts());
							String childLabel = findLabel(childNid, chartContext.getScopedConcepts());

							row.put("childId", String.valueOf(childNid));
							row.put("parentId", String.valueOf(parentNid));
							row.put("childLabel", childLabel);
							row.put("parentLabel", parentLabel);
							row.put("constellationId", chart.constellationId().toString());
							row.put("relLabel", "IS_A");
							row.put("relType", "Is-a");

							if (parentLabel.equals("Concept")) {
								row.put("conceptName", chart.languageCalculator().getDescriptionTextOrNid(parentNid));
							}

							batch.add(row);
							if (batch.size() == batchSize) {
								neo4jClient.query(cypherQuery)
										.bind(batch)
										.to("batch")
										.run();
								chartContext.reportProgress(getStep(), batch.size());
								batch.clear();
							}
							if (!batch.isEmpty()) {
								neo4jClient.query(cypherQuery)
										.bind(batch)
										.to("batch")
										.run();
								chartContext.reportProgress(getStep(), batch.size());
							}
						}
					});
				});
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
}
