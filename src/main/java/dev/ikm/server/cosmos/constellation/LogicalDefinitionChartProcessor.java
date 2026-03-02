package dev.ikm.server.cosmos.constellation;

import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.TinkarTermV2;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogicalDefinitionChartProcessor implements ChartProcessor {

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
		return Step.PROCESS_LOGICAL_DEFINITIONS;
	}

	@Override
	public String getProcessorName() {
		return "Logical Definition Charting";
	}

	@Override
	public void process(ChartingContext chartContext, int batchSize) {
		Chart chart = chartContext.getChart();
		Neo4jClient neo4jClient = chartContext.getNeo4jClient();
		StampCalculator stampCalculator = chart.stampCalculator();

		chartContext.getScopedConcepts().values()
				.stream()
				.flatMap(List::stream)
				.forEach(nid -> {
					List<Map<String, Object>> batch = new ArrayList<>();
					stampCalculator.forEachSemanticVersionForComponentOfPattern(nid, TinkarTermV2.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid(),
							(semanticEntityVersion, entityVersion, patternEntityVersion) -> {
								stampCalculator.getFieldForSemanticWithMeaning(semanticEntityVersion, TinkarTermV2.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS).ifPresent(field -> {
									DiTreeEntity diTreeEntity = (DiTreeEntity) field.value();

									System.out.println("break");
								});
							});

					Map<String, Object> row = new HashMap<>();
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
				});
	}
}
