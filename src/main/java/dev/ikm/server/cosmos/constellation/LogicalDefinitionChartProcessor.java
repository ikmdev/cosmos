package dev.ikm.server.cosmos.constellation;

import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.TinkarTermV2;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LogicalDefinitionChartProcessor implements ChartProcessor {

	/*
		Core Principles
			1) Necessary conditions → Attach directly to Concept (they always apply)
			2) Multiple sufficient sets → Create intermediate nodes (they're alternatives)
			3) Single sufficient set → Flatten to Concept (no alternatives = no need for container)
			4) Role groups → Always create RoleGroupQualifier nodes (preserve grouping)

			//Pseudocode
			if (concept.hasNecessaryConditions()) {
				attachNecessaryConditions(concept);
			}

			if (concept.getSufficientSets().size() > 1) {
				// Multiple alternatives - create intermediate nodes
				for (SufficientSet ss : concept.getSufficientSets()) {
					createSufficientSetNode(concept, ss);
				}
			} else if (concept.getSufficientSets().size() == 1) {
				// Single sufficient set - attach role groups directly to concept
				attachRoleGroupsDirectly(concept, concept.getSufficientSets().get(0));
			}
	 */

	List<Map<String, Object>> sufficientSetMaps = new ArrayList<>();

	private enum DefinitionType {
		NECESSARY,
		SUFFICIENT,
		UNKNOWN
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

									//Are there multiple definitions coming from the root node?
									if (diTreeEntity.successorMap().get(diTreeEntity.root().vertexIndex()).size() > 1) {
										//NOP
									} else {
										singleDefinition(diTreeEntity, nid, chartContext);
									}
								});
							});
				});
	}

	private void singleDefinition(DiTreeEntity diTreeEntity, int referenceNid, ChartingContext chartingContext) {
		int definitionIndex = diTreeEntity.successorMap().get(diTreeEntity.root().vertexIndex()).get(0);
		EntityVertex definitionVertex = diTreeEntity.vertexMap().get(definitionIndex);
		DefinitionType definitionType = getDefinitionType(definitionVertex);
		if (definitionType == DefinitionType.UNKNOWN) {
			throw new IllegalStateException("Unknown (single) definition type");
		}

		//All items: 1) Concept References, 2) Role Groups (and roles), 3) Roles are stored in the add successor map
		int andIndex = diTreeEntity.successorMap().get(definitionVertex.vertexIndex()).get(0);
		EntityVertex andVertex = diTreeEntity.vertexMap().get(andIndex);
		if (andVertex.getMeaningNid() != TinkarTermV2.AND.nid()) {
			throw new IllegalStateException("Expected AND node in single definition");
		}

		//Figure out what's in the Definition Set
		List<Integer> roleGroupIndices = new ArrayList<>();
		List<Integer> roleIndices = new ArrayList<>();
		List<Integer> referenceIndices = new ArrayList<>();
		int[] andSuccessors = diTreeEntity.successorMap().get(andIndex).toArray();
		for (int andSuccessor : andSuccessors) {
			EntityVertex andSuccessorVertex = diTreeEntity.vertexMap().get(andSuccessor);
			//Check if the meaning is Concept Reference (aka Is-A)
			if (andSuccessorVertex.getMeaningNid() == TinkarTermV2.CONCEPT_REFERENCE.nid()) {
				referenceIndices.add(andSuccessor);
			} else if (andSuccessorVertex.getMeaningNid() == TinkarTermV2.ROLE.nid()) {
				//Need to see if the Role Type property is Role Group or a Role constraint
				Concept[] conceptProperties = diTreeEntity.vertexMap().get(andSuccessor).properties().values().toArray(new Concept[0]);
				if (conceptProperties.length != 2) {
					throw new IllegalStateException("Expected 2 properties for Role");
				}
				if (conceptProperties[0].nid() == TinkarTermV2.ROLE_GROUP.nid()) {
					roleGroupIndices.add(andSuccessor);
				} else {
					roleIndices.add(andSuccessor);
				}
			}
		}


		//Parse out role properties for graph
		if (roleGroupIndices.isEmpty()) {
			if (definitionType == DefinitionType.SUFFICIENT) {
				//write sufficient intermediate node to concept
				//write roles to intermediate node
			} else {
				//write roles to concept
			}
		} else {
			if (definitionType == DefinitionType.SUFFICIENT) {
				//write sufficient intermediate node to concept
				writeSufficientIntermediateNode(diTreeEntity, chartingContext.getChart(), 1, 0, roleGroupIndices, referenceNid);
				//write role groups to intermediate node
				//write roles to intermediate node
			} else {
				//write role groups intermediate node to concept
				//write roles to intermediate node
			}
		}
	}

	private UUID writeSufficientIntermediateNode(DiTreeEntity diTreeEntity, Chart chart, int totalDefinitions, int definitionIndex, List<Integer> roleGroups, int conceptId) {
		UUID sufficientIntermediateNodeId = UUID.randomUUID();

		Map<String, Object> row = new HashMap<>();

		//Concept Look up information
		row.put("conceptId", conceptId);

		//Sufficient Set Node
		row.put("definitionId", sufficientIntermediateNodeId.toString());
		row.put("constellationId", chart.constellationId().toString());
		row.put("definitionIndex", definitionIndex);
		row.put("definitionType", "Sufficient condition");
		row.put("logicalOperator", "And within definition");
		row.put("completeness", "Complete sufficient set");
		row.put("roleGroupCount", roleGroups.size());

		StringBuilder sb = new StringBuilder();
		for (int roleGroupIndex : roleGroups) {
			int andIndex = diTreeEntity.successorMap().get(roleGroupIndex).get(0);
			EntityVertex andVertex = diTreeEntity.vertexMap().get(andIndex);
			if (andVertex.getMeaningNid() != TinkarTermV2.AND.nid()) {
				throw new IllegalStateException("Expected AND node in role group in sufficient definition");
			}
			int[] andSuccessors = diTreeEntity.successors(andIndex).toArray();
			for (int andSuccessor : andSuccessors) {
				Concept[] roleProperties = diTreeEntity.vertexMap().get(andSuccessor).properties().values().toArray(new Concept[0]);
				if (roleProperties.length != 2) {
					throw new IllegalStateException("Expected 2 properties for Role");
				}
				Concept roleConcept = roleProperties[0];
				int roleReferenceIndex = diTreeEntity.successorMap().get(andSuccessor).get(0);
				EntityVertex roleReferenceVertex = diTreeEntity.vertexMap().get(roleReferenceIndex);
				if (roleReferenceVertex.getMeaningNid() != TinkarTermV2.CONCEPT_REFERENCE.nid()) {
					throw new IllegalStateException("Expected Concept Reference in role group in sufficient definition");
				}
				Concept[] fillerProperties = diTreeEntity.vertexMap().get(roleReferenceIndex).properties().values().toArray(new Concept[0]);
				if (roleProperties.length != 2) {
					throw new IllegalStateException("Expected 2 properties for Role");
				}
				Concept fillerConcept = fillerProperties[0];

				sb.append(roleConcept.description() + "+" + fillerConcept.description() + "|");
			}

		}
		row.put("roleGroupSignature", sb.substring(0, sb.length() - 1));

		//Relationship Between Concept and Sufficient Set Node
		row.put("relLabel", "HAS_SUFFICIENT_DEFINITION");
		row.put("alternativeNumber", totalDefinitions);
		row.put("logicalRole", "Or alternative");
		row.put("sufficientWhen", "All contained conditions met");

		sufficientSetMaps.add(row);

		return sufficientIntermediateNodeId;
	}

	private void writeRoleGroup(DiTreeEntity diTreeEntity, List<Integer> roleGroups) {
		Map<String, Object> row = new HashMap<>();

		//Relationship Properties between Sufficient Intermediate Node
		row.put("groupNumber", 0);
		row.put("logicalOperator", "And");
	}

	private void writeRoles(DiTreeEntity diTreeEntity, List<Integer> roles) {

	}

	private DefinitionType getDefinitionType(EntityVertex entityVertex) {
		DefinitionType definitionType = DefinitionType.UNKNOWN;
		if (entityVertex.getMeaningNid() == TinkarTermV2.NECESSARY_SET.nid()) {
			definitionType = DefinitionType.NECESSARY;
		} else if (entityVertex.getMeaningNid() == TinkarTermV2.SUFFICIENT_SET.nid()) {
			definitionType = DefinitionType.SUFFICIENT;
		}
		return definitionType;
	}
}
