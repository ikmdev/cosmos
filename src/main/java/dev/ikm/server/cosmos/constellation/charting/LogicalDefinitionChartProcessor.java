package dev.ikm.server.cosmos.constellation.charting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.ikm.server.cosmos.constellation.Chart;
import dev.ikm.server.cosmos.constellation.Step;
import dev.ikm.server.cosmos.constellation.definition.Clause;
import dev.ikm.server.cosmos.constellation.definition.Definition;
import dev.ikm.server.cosmos.constellation.definition.Role;
import dev.ikm.server.cosmos.constellation.definition.RoleGroup;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.TinkarTermV2;

@Component
public class LogicalDefinitionChartProcessor extends BaseChartProcessor {

	private record Clutch(
			List<Map<String, Object>> roleNodeBatch,
			List<Map<String, Object>> roleRelationshipBatch,
			List<Map<String, Object>> roleGroupIntermediateNodeBatch,
			List<Map<String, Object>> roleGroupIntermediateRelationshipBatch,
			List<Map<String, Object>> sufficientIntermediateNodeBatch,
			List<Map<String, Object>> sufficientIntermediateRelationshipBatch) {
	}

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
		Clutch clutch = new Clutch(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		StampCalculator stampCalculator = chartingContext.getChart().stampCalculator();

		chartingContext.getScopedConcepts().values()
				.stream()
				.flatMap(Set::stream)
				.forEach(nid -> {
					PrimitiveData.get().forEachSemanticNidForComponentOfPattern(nid, TinkarTermV2.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid(), semanticNid -> {
						Latest<EntityVersion> latest = stampCalculator.latest(semanticNid);
						if (latest.isPresent()) {
							SemanticEntityVersion semanticEntityVersion = (SemanticEntityVersion) latest.get();
							stampCalculator.getFieldForSemanticWithMeaning(semanticEntityVersion,
									TinkarTermV2.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS).ifPresent(field -> {
								final DiTreeEntity diTreeEntity = (DiTreeEntity) field.value();
								LogicalDefinitionParser ldp = new LogicalDefinitionParser(diTreeEntity);
								Definition definition = ldp.parse();
								writeDefinitions(nid, chartingContext, definition, clutch);
							});
						}
					});
				});
	}

	private void writeDefinitions(int conceptNid, ChartingContext chartingContext, Definition definition, Clutch clutch) {
		String parentId = String.valueOf(conceptNid);
		definition.sets().forEach(clause -> {
			switch (clause.element()) {
				case NECESSARY_SET -> processNecessaryDefinition(parentId, clause, chartingContext, clutch);
				case SUFFICIENT_SET ->
						processSufficientDefinition(parentId, clause, definition.sufficientSetCount(), chartingContext, clutch);
				default -> throw new IllegalStateException("Unknown definition type");
			}
		});
	}

	private void processNecessaryDefinition(String originId, Clause clause, ChartingContext chartingContext, Clutch clutch) {
		writeRoles(originId, clause.roles(), chartingContext, clutch);
		processRoleGroups(originId, clause.roleGroups(), chartingContext, clutch);
		//Skip Reference Concepts. Those are handled in the Hiearchy Chart Processor
	}

	private void processSufficientDefinition(String originId, Clause clause, long sufficientCount, ChartingContext chartingContext, Clutch clutch) {
		if (sufficientCount == 1) {
			writeRoles(originId, clause.roles(), chartingContext, clutch);
			processRoleGroups(originId, clause.roleGroups(), chartingContext, clutch);
		} else {
			//Write Sufficient Intermediate Node
			String intermediateId = UUID.randomUUID().toString();
			writeSufficientIntermediateNode(originId, intermediateId, clause, chartingContext, clutch);
			//Write Roles
			writeRoles(intermediateId, clause.roles(), chartingContext, clutch);
			//Write Role Group Nodes
			processRoleGroups(intermediateId, clause.roleGroups(), chartingContext, clutch);
		}
	}

	private void processRoleGroups(String originId, List<RoleGroup> roleGroups, ChartingContext chartingContext, Clutch clutch) {
		roleGroups.forEach(roleGroup -> {
			if (roleGroup.roles().size() > 1) {
				//Write Role Group Intermediate Node (via parent)
				String intermediateNodeId = UUID.randomUUID().toString();
				writeRoleGroupIntermediateNode(intermediateNodeId, originId, chartingContext, clutch);
				//Write roles to intermediate node
				writeRoles(intermediateNodeId, roleGroup.roles(), chartingContext, clutch);
			} else {
				//Write roles to origin node
				writeRoles(originId, roleGroup.roles(), chartingContext, clutch);
			}
		});
	}

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

			String destinationId = String.valueOf(role.reference().concept().nid());
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
				Map<String, Object> outOfScopeRow = new HashMap<>();
				outOfScopeRow.put("id", destinationId);
				outOfScopeRow.put("label", "Concept");
				outOfScopeRow.put("name", chart.languageCalculator().getDescriptionTextOrNid(Integer.parseInt(destinationId)));
				outOfScopeRow.put("constellationId", chart.constellationId().toString());
				clutch.roleNodeBatch.add(outOfScopeRow);
			}

			clutch.roleRelationshipBatch().add(row);
		});
	}

	private void writeSufficientIntermediateNode(String intermediateNodeId, String originId, Clause clause, ChartingContext chartingContext, Clutch clutch) {
		Chart chart = chartingContext.getChart();

		//Sufficient Set Node
		Map<String, Object> sufficientIntermediateNodeRow = new HashMap<>();
		sufficientIntermediateNodeRow.put("label", "SufficientDefinition");
		sufficientIntermediateNodeRow.put("id", intermediateNodeId);
		sufficientIntermediateNodeRow.put("constellationId", chart.constellationId().toString());
		sufficientIntermediateNodeRow.put("definitionType", "Sufficient condition");
		sufficientIntermediateNodeRow.put("logicalOperator", "And within definition");
		sufficientIntermediateNodeRow.put("completeness", "Complete sufficient set");
		clutch.sufficientIntermediateNodeBatch().add(sufficientIntermediateNodeRow);

		//Relationship Between Concept and Sufficient Set Node
		Map<String, Object> sufficientIntermediateRelationshipRow = new HashMap<>();
		String originLabel = findLabel(originId, chartingContext.getScopedConcepts());
		sufficientIntermediateRelationshipRow.put("label", "SufficientDefinition");
		sufficientIntermediateNodeRow.put("id", intermediateNodeId);
		sufficientIntermediateNodeRow.put("constellationId", chart.constellationId().toString());
		sufficientIntermediateRelationshipRow.put("originId", originId);
		sufficientIntermediateRelationshipRow.put("originLabel", originLabel);
		sufficientIntermediateRelationshipRow.put("relLabel", "HAS_SUFFICIENT_DEFINITION");
		sufficientIntermediateRelationshipRow.put("relType", "Has Sufficient Definition");
		sufficientIntermediateRelationshipRow.put("logicalRole", "Or alternative");
		sufficientIntermediateRelationshipRow.put("sufficientWhen", "All contained conditions met");

		clutch.sufficientIntermediateRelationshipBatch().add(sufficientIntermediateRelationshipRow);
	}

	private void writeRoleGroupIntermediateNode(String nodeId, String originId, ChartingContext chartingContext, Clutch clutch) {
		Chart chart = chartingContext.getChart();
		String originLabel;
		//Origin maybe a valid node (i.e., nid) but could also be to a role group (i.e., UUID)
		if (isNid(originId)) {
			originLabel = findLabel(originId, chartingContext.getScopedConcepts());
		} else {
			originLabel = "SufficientDefinition";
		}

		//Role Group Intermediate Node
		Map<String, Object> roleGroupIntermediateNodeRow = new HashMap<>();
		roleGroupIntermediateNodeRow.put("id", nodeId);
		roleGroupIntermediateNodeRow.put("label", "RoleGroupQualifier");
		roleGroupIntermediateNodeRow.put("constellationId", chart.constellationId().toString());
		roleGroupIntermediateNodeRow.put("logicalOperator", "And");
		clutch.roleNodeBatch.add(roleGroupIntermediateNodeRow);

		//Relationship Properties between Sufficient Intermediate Node
		Map<String, Object> roleGroupIntermediateRelationshipRow = new HashMap<>();
		roleGroupIntermediateRelationshipRow.put("label", "RoleGroupQualifier");
		roleGroupIntermediateRelationshipRow.put("id", nodeId);
		roleGroupIntermediateRelationshipRow.put("constellationId", chart.constellationId().toString());
		roleGroupIntermediateRelationshipRow.put("originId", originId);
		roleGroupIntermediateRelationshipRow.put("originLabel", originLabel);
		roleGroupIntermediateRelationshipRow.put("relLabel", "CONTAINS_QUALIFIER_GROUP");
		roleGroupIntermediateRelationshipRow.put("relType", "Contains Qualifier Group");
		clutch.roleGroupIntermediateRelationshipBatch().add(roleGroupIntermediateRelationshipRow);
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
}
