package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.constellation.LogicalDefinitionParser.Definition;
import dev.ikm.server.cosmos.constellation.LogicalDefinitionParser.LogicalDefinition;
import dev.ikm.server.cosmos.constellation.LogicalDefinitionParser.Role;
import dev.ikm.server.cosmos.constellation.LogicalDefinitionParser.RoleGroup;
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.TinkarTermV2;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/***
 * Core Principles
 * 	1) Necessary conditions → Attach directly to Concept (they always apply)
 * 	2) Multiple sufficient sets → Create intermediate nodes (they're alternatives)
 * 	3) Single sufficient set → Flatten to Concept (no alternatives = no need for container)
 * 	4) Role groups → Always create RoleGroupQualifier nodes (preserve grouping)
 * */
public class LogicalDefinitionChartProcessor implements ChartProcessor {

	private record Clutch(
			List<Map<String, Object>> roleBatch,
			List<Map<String, Object>> roleGroupIntermediateBatch,
			List<Map<String, Object>> sufficientIntermediateBatch) {}

	@Override
	public Step getStep() {
		return Step.PROCESS_LOGICAL_DEFINITIONS;
	}

	@Override
	public String getProcessorName() {
		return "Logical Definition Charting";
	}

	@Override
	public void process(ChartingContext chartingContext, int batchSize) {
		Clutch clutch = new Clutch(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		StampCalculator stampCalculator = chartingContext.getChart().stampCalculator();

		chartingContext.getScopedConcepts().values()
				.stream()
				.flatMap(List::stream)
				.forEach(nid -> {
					List<Map<String, Object>> batch = new ArrayList<>();
					stampCalculator.forEachSemanticVersionForComponentOfPattern(nid, TinkarTermV2.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid(),
							(semanticEntityVersion, entityVersion, patternEntityVersion) -> {
								stampCalculator.getFieldForSemanticWithMeaning(semanticEntityVersion,
										TinkarTermV2.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS).ifPresent(field -> {
									final DiTreeEntity diTreeEntity = (DiTreeEntity) field.value();
									LogicalDefinitionParser ldp = new LogicalDefinitionParser(diTreeEntity);
									LogicalDefinition logicalDefinition = ldp.parse();
									writeDefinitions(nid, chartingContext, logicalDefinition, clutch);
								});
							});
				});
		batchWrite(sufficientIntermediateNodeQuery, clutch.sufficientIntermediateBatch(), chartingContext, batchSize);
		batchWrite(roleGroupIntermediateNodeQuery, clutch.roleGroupIntermediateBatch(), chartingContext, batchSize);
		batchWrite(roleQuery, clutch.roleBatch(), chartingContext, batchSize);
	}

	private void writeDefinitions(int conceptNid, ChartingContext chartingContext, LogicalDefinition logicalDefinition, Clutch clutch) {
		String parentId = String.valueOf(conceptNid);
		logicalDefinition.definitions().forEach(definition -> {
			switch (definition.type()) {
				case NECESSARY -> processNecessaryDefinition(parentId, definition, chartingContext, clutch);
				case SUFFICIENT -> processSufficientDefinition(parentId, definition, logicalDefinition.sufficientCount(), chartingContext, clutch);
				default -> throw new IllegalStateException("Unknown definition type");
			}
		});
	}

	private void processNecessaryDefinition(String originId, Definition definition, ChartingContext chartingContext, Clutch clutch) {
		writeRoles(originId, definition.roles(), chartingContext, clutch);
		processRoleGroups(originId, definition.roleGroups(), chartingContext, clutch);
		//Skip Reference Concepts. Those are handled in the Hiearchy Chart Processor
	}

	private void processSufficientDefinition(String originId, Definition definition, long sufficientCount, ChartingContext chartingContext, Clutch clutch) {
		if (sufficientCount == 1) {
			writeRoles(originId, definition.roles(), chartingContext, clutch);
			processRoleGroups(originId, definition.roleGroups(), chartingContext, clutch);
		} else {
			//Write Sufficient Intermediate Node
			String intermediateId = UUID.randomUUID().toString();
			writeSufficientIntermediateNode(originId, intermediateId, definition, chartingContext, clutch);
			//Write Roles
			writeRoles(intermediateId, definition.roles(), chartingContext, clutch);
			//Write Role Group Nodes
			processRoleGroups(intermediateId, definition.roleGroups(), chartingContext, clutch);
		}
	}

	private void processRoleGroups(String originId, List<RoleGroup> roleGroups, ChartingContext chartingContext, Clutch clutch) {
		roleGroups.forEach(roleGroup -> {
			//Write Role Group Intermediate Node (via parent)
			String intermediateNodeId = UUID.randomUUID().toString();
			writeRoleGroupIntermediateNode(intermediateNodeId, originId, chartingContext, clutch);
			//Write roles to intermediate node
			writeRoles(intermediateNodeId, roleGroup.roles(), chartingContext, clutch);
		});
	}

	private final String roleQuery = """
		  UNWIND $batch AS row
		  MATCH (origin:$(row.originLabel) {id: row.originId, constellationId: row.constellationId})

		  OPTIONAL MATCH (destination:$(row.destinationLabel) {id: row.destinationId, constellationId: row.constellationId})

		  // Find or create the generic node
		  MERGE (generic:Concept {id: row.genericId, constellationId: row.constellationId})
			ON CREATE SET generic.name = coalesce(row.conceptName, row.genericName)

		  WITH origin, row, coalesce(destination, generic) AS destinationNode

		  MERGE (origin)-[r:$(row.relLabel) {type: row.relType, constellationId: row.constellationId}]->(destinationNode)
		  """;

	private void writeRoles(String originId, List<Role> roles, ChartingContext chartingContext, Clutch clutch) {
		Chart chart = chartingContext.getChart();
		roles.forEach(role -> {
			Map<String, Object> row = new HashMap<>();

			String originLabel;
			//Origin maybe a valid node (i.e., nid) but could also be to a role group (i.e., UUID)
			if (isNid(originId)) {
				originLabel = findLabel(originId, chartingContext.getScopedConcepts());
			} else {
				originLabel = "RoleGroupQualifier";
			}

			String destinationId = String.valueOf(role.object().nid());
			String destinationLabel = findLabel(destinationId, chartingContext.getScopedConcepts());

			row.put("originId", originId);
			row.put("destinationId", destinationId);
			row.put("originLabel", originLabel);
			row.put("destinationLabel", destinationLabel);
			row.put("constellationId", chart.constellationId().toString());

			String relationshipText = chart.languageCalculator().getDescriptionTextOrNid(role.predicate());
			String relationshipLabel = relationshipText.replaceAll("[^a-zA-Z0-9]", "_");

			row.put("relLabel", relationshipLabel);
			row.put("relType", relationshipText);

			if (destinationLabel.equals("Concept")) {
				row.put("conceptName", chart.languageCalculator().getDescriptionTextOrNid(Integer.parseInt(destinationId)));
				row.put("genericId", destinationId);
			}

			clutch.roleBatch().add(row);
		});
	}

	private final String sufficientIntermediateNodeQuery = """
		  UNWIND $batch AS row
		  MERGE (sin:$(row.label) {id: row.id, constellationId: row.constellationId})
		  SET sin += row.properties
		  
		  // Bridge the SET and MATCH clauses
		  WITH row, sin
		  
		  MATCH (origin:$(row.originLabel) {id: row.originId, constellationId: row.constellationId})
		  MERGE (origin)-[r:$(row.relLabel) {type: row.relType, constellationId: row.constellationId}]->(sin)
		  """;

	private void writeSufficientIntermediateNode(String intermediateNodeId, String originId, Definition definition, ChartingContext chartingContext, Clutch clutch) {
		Chart chart = chartingContext.getChart();
		Map<String, Object> row = new HashMap<>();

		//Sufficient Set Node
		row.put("label", "SufficientDefinition");
		row.put("id", intermediateNodeId);
		row.put("constellationId", chart.constellationId().toString());
//		row.put("definitionIndex", definitionIndex); //TODO - do we need this
		row.put("definitionType", "Sufficient condition");
		row.put("logicalOperator", "And within definition");
		row.put("completeness", "Complete sufficient set");
//		row.put("roleGroupCount", roleGroups.size());
//		row.put("roleGroupSignature", buildRoleGroupSignature(diTreeEntity, roleGroups));

		//Relationship Between Concept and Sufficient Set Node
		String originLabel = findLabel(originId, chartingContext.getScopedConcepts());
		row.put("originId", originId);
		row.put("originLabel", originLabel);
		row.put("relLabel", "HAS_SUFFICIENT_DEFINITION");
		row.put("relType", "Has Sufficient Definition");
//		row.put("alternativeNumber", totalDefinitions); //TODO - do we need this
		row.put("logicalRole", "Or alternative");
		row.put("sufficientWhen", "All contained conditions met");

		clutch.sufficientIntermediateBatch().add(row);
	}

	private final String roleGroupIntermediateNodeQuery = """
			UNWIND $batch AS row
			MERGE (rgin:$(row.label) {id: row.id, constellationId: row.constellationId})

			// Explicitly set only the 'data' properties
			SET rgin += { groupNumber: row.groupNumber, logicalOperator: row.logicalOperator }

			WITH row, rgin
			MATCH (origin:$(row.originLabel) {id: row.originId, constellationId: row.constellationId})
			MERGE (origin)-[r:$(row.relLabel) {type: row.relType, constellationId: row.constellationId}]->(rgin)
		  """;
	private void writeRoleGroupIntermediateNode(String nodeId, String originId, ChartingContext chartingContext, Clutch clutch) {
		Chart chart = chartingContext.getChart();
		Map<String, Object> row = new HashMap<>();

		String originLabel;
		//Origin maybe a valid node (i.e., nid) but could also be to a role group (i.e., UUID)
		if (isNid(originId)){
			originLabel = findLabel(originId, chartingContext.getScopedConcepts());
		} else {
			originLabel = "SufficientDefinition";
		}

		//Relationship Properties between Sufficient Intermediate Node
		row.put("label", "RoleGroupQualifier");
		row.put("id", nodeId);
		row.put("constellationId", chart.constellationId().toString());
		row.put("groupNumber", 0); //TODO - do we need this?
		row.put("logicalOperator", "And");
		row.put("originId", originId);
		row.put("originLabel", originLabel);
		row.put("relLabel", "CONTAINS_QUALIFIER_GROUP");
		row.put("relType", "Contains Qualifier Group");

		clutch.roleGroupIntermediateBatch().add(row);
	}

	private String findLabel(String nid, Map<Facade, List<Integer>> scopedConcepts) {
		int conceptNid = Integer.parseInt(nid);
		for (Map.Entry<Facade, List<Integer>> entry : scopedConcepts.entrySet()) {
			if (entry.getValue().contains(conceptNid)) {
				return entry.getKey().name().replaceAll("[^a-zA-Z0-9]", "");
			}
		}
		return "Concept";
	}

	private boolean isNid(String id) {
		if (id == null) {
			return false;
		}
		try {
			Integer.parseInt(id);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private void batchWrite(String query, List<Map<String, Object>> data, ChartingContext chartingContext, int batchSize) {
		List<Map<String, Object>> batch = new ArrayList<>();
		Neo4jClient neo4jClient = chartingContext.getNeo4jClient();
		if (data.isEmpty()) {
			return;
		}

		for (Map<String, Object> datum : data) {
			batch.add(datum);
			if (batch.size() == batchSize) {
				neo4jClient.query(query)
						.bind(batch)
						.to("batch")
						.run();
				chartingContext.reportProgress(getStep(), batch.size());
				batch.clear();
			}
		}
		if (!batch.isEmpty()) {
			neo4jClient.query(query)
					.bind(batch)
					.to("batch")
					.run();
			chartingContext.reportProgress(getStep(), batch.size());
		}
	}
}
