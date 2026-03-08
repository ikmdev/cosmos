package dev.ikm.server.cosmos.constellation.charting;

import dev.ikm.server.cosmos.constellation.Step;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicIdList;
import dev.ikm.tinkar.common.id.PublicIdSet;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.coordinate.language.calculator.LanguageCalculator;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTermV2;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SemanticChartProcessor implements ChartProcessor {


	private record relationship(String relLabel, String relType, int destination) {
	}

	private final String semanticRelationshipQuery = """
			UNWIND $batch AS row
			MATCH (origin:$(row.originLabel) {id: row.originId, constellationId: row.constellationId})
			MATCH (semantic:$(row.semanticLabel) {id: row.semanticId, constellationId: row.constellationId})
			MERGE (origin)-[r:$(row.relLabel) {type: row.relType, constellationId: row.constellationId}]->(semantic)""";

	private final String semanticFieldRelationships = """
			UNWIND $batch AS row
			MATCH (semantic:$(row.semanticLabel) {id: row.semanticId, constellationId: row.constellationId})
			MERGE (destination:$(row.destinationLabel) {id: row.destinationId, constellationId: row.constellationId})
			MERGE (semantic)-[r:$(row.relLabel) {type: row.relType, constellationId: row.constellationId}]->(destination)""";

	private final String semanticNodeQuery = """
			UNWIND $batch AS row
			MERGE (n:$(row.label) {id: row.id, constellationId: row.constellationId})
			SET n += row.props
			""";

	private final String outOfScopeCreateConceptQuery = """
			UNWIND $batch AS row
			MERGE (n:$(row.label) {id: row.id, constellationId: row.constellationId})
			SET n.name = row.name
			""";

	private List<Integer> excludedPatternNids() {
		return List.of(
				TinkarTermV2.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid(),
				TinkarTermV2.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
				TinkarTermV2.DESCRIPTION_PATTERN.nid(),
				TinkarTermV2.OWL_AXIOM_SYNTAX_PATTERN.nid());
	}

	@Override
	public Step getStep() {
		return Step.PROCESS_SEMANTICS;
	}

	@Override
	public String getProcessorName() {
		return "Semantic Charting";
	}

	@Override
	public void process(ChartingContext chartingContext, int batchSize) {
		List<Map<String, Object>> semanticNodeData = new ArrayList<>();
		List<Map<String, Object>> outOfScopeData = new ArrayList<>();
		List<Map<String, Object>> semanticRelationshipsData = new ArrayList<>();
		List<Map<String, Object>> semanticFieldRelationshipsData = new ArrayList<>();


		StampCalculator stampCalculator = chartingContext.getChart().stampCalculator();
		LanguageCalculator languageCalculator = chartingContext.getChart().languageCalculator();
		Set<Integer> includedModules = chartingContext.getChart().includedModules()
				.stream().map(facade -> facade.id().nid()).collect(Collectors.toSet());
		Set<Integer> excludedModules = chartingContext.getChart().excludedModules()
				.stream().map(facade -> facade.id().nid()).collect(Collectors.toSet());

		chartingContext.getScopedConcepts().forEach((facade, descendants) -> {
			descendants.forEach(nid -> {
				PrimitiveData.get().forEachSemanticNidForComponent(nid, semanticNid -> {
					Latest<EntityVersion> latest = stampCalculator.latest(semanticNid);
					processSemanticVersion(latest, nid, includedModules, excludedModules, chartingContext,
							stampCalculator, languageCalculator, semanticNodeData, outOfScopeData,
							semanticRelationshipsData, semanticFieldRelationshipsData);
				});
			});
		});

		writeData(semanticNodeQuery, semanticNodeData, chartingContext, batchSize);
		writeData(outOfScopeCreateConceptQuery, outOfScopeData, chartingContext, batchSize);
		writeData(semanticRelationshipQuery, semanticRelationshipsData, chartingContext, batchSize);
		writeData(semanticFieldRelationships, semanticFieldRelationshipsData, chartingContext, batchSize);
	}

	private void processSemanticVersion(Latest<EntityVersion> latest,
										int refNid,
										Set<Integer> includedModules,
										Set<Integer> excludedModules,
										ChartingContext chartingContext,
										StampCalculator stampCalculator,
										LanguageCalculator languageCalculator,
										List<Map<String, Object>> semanticNodeData,
										List<Map<String, Object>> outOfScopeData,
										List<Map<String, Object>> semanticRelationshipsData,
										List<Map<String, Object>> semanticFieldRelationshipsData) {
		if (latest.isPresent()) {
			SemanticEntityVersion semanticEntityVersion = (SemanticEntityVersion) latest.get();
			int stampModuleNid = semanticEntityVersion.moduleNid();

			if (includedModules.contains(stampModuleNid) &&
					!excludedModules.contains(stampModuleNid) &&
					!excludedPatternNids().contains(semanticEntityVersion.pattern().nid())) {

				//This Semantic is within the allowed modules to be charted
				String semanticId = String.valueOf(semanticEntityVersion.nid());
				int patternNid = semanticEntityVersion.pattern().nid();
				Latest<PatternEntityVersion> latestPattern = stampCalculator.latest(patternNid);
				if (latestPattern.isPresent()) {
					Map<String, Object> semanticNodeProps = new HashMap<>();
					PatternEntityVersion patternEntityVersion = latestPattern.get();

					//Get label for Semantic Node
					String semanticMeaningDescription = languageCalculator.getDescriptionTextOrNid(patternEntityVersion.semanticMeaningNid());
					String semanticLabel = createSemanticLabel(semanticMeaningDescription);
					if (semanticLabel.isEmpty()) {
						semanticLabel = "Semantic";
					}

					for (int idx = 0; idx < semanticEntityVersion.fieldValues().size(); idx++) {
						Object value = semanticEntityVersion.fieldValues().get(idx);
						//Process for new Semantic Node
						int fieldMeaningNid = patternEntityVersion.fieldDefinitions().get(semanticEntityVersion.fieldValues().indexOf(value)).meaningNid();
						String fieldLabel = "ERROR";
						if (semanticEntityVersion.patternNid() == TinkarTermV2.IDENTIFIER_PATTERN.nid()) {
							Latest<Field<Object>> latestField = stampCalculator.getFieldForSemanticWithMeaning(semanticEntityVersion, TinkarTermV2.IDENTIFIER_SOURCE);
							if (latestField.isPresent()) {
								EntityProxy sourceConcept = (EntityProxy) latestField.get().value();
								fieldLabel = languageCalculator.getDescriptionTextOrNid(sourceConcept);
							}
						} else {
							fieldLabel = languageCalculator.getDescriptionTextOrNid(fieldMeaningNid);
						}
						processFieldForProps(semanticNodeProps, fieldLabel, value);

						//Process for relationships to other nodes for Semantic Node
						Set<Integer> destinationIds = processFieldForRelationships(value);
						for (int destinationNid : destinationIds) {
							String destinationId = String.valueOf(destinationNid);
							String destinationLabel = findLabel(destinationId, chartingContext.getScopedConcepts());
							if (destinationLabel.equals("Concept")) {
								String name = chartingContext.getChart().languageCalculator().getDescriptionTextOrNid(destinationNid);
								collectOutOfScopeConceptRows(destinationId, "Concept", name, chartingContext.getChart().constellationId().toString(), outOfScopeData);
							}
							String relType = languageCalculator.getDescriptionTextOrNid(fieldMeaningNid);
							String relLabel = relType.replaceAll("[^a-zA-Z0-9]", "_");
							collectSemanticFieldRelationshipRows(semanticId, semanticLabel, destinationId, destinationLabel, relLabel, relType, chartingContext.getChart().constellationId().toString(), semanticFieldRelationshipsData);
						}
					}

					collectSemanticNodeRows(semanticId, semanticLabel, chartingContext.getChart().constellationId().toString(), semanticNodeProps, semanticNodeData);

					//Connect Node to Semantic Node
					String originId = String.valueOf(refNid);
					String originLabel = findLabel(originId, chartingContext.getScopedConcepts());
					collectSemanticRelationshipRows(originId, originLabel, semanticId, semanticLabel, chartingContext.getChart().constellationId().toString(), semanticRelationshipsData);
				}
			}
		}
	}

	private String createSemanticLabel(String semanticDescription) {
		return  Arrays.stream(semanticDescription.split("\\s+"))
				.filter(word -> !word.isEmpty())
				.map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
				.collect(Collectors.joining(""));
	}

	private Set<Integer> processFieldForRelationships(Object value) {
		if (value instanceof PublicIdList<?> publicIdList) {
			return publicIdList.stream()
					.map(Entity::nid)
					.collect(Collectors.toSet());
		} else if (value instanceof PublicIdSet<?> publicIdSet) {
			return publicIdSet.stream()
					.map(Entity::nid)
					.collect(Collectors.toSet());
		} else if (value instanceof IntIdList intIdList) {
			return intIdList.mapToSet(id -> id);
		} else if (value instanceof IntIdSet intIdSet) {
			return intIdSet.mapToSet(id -> id);
		} else if (value instanceof Component component) {
			return Set.of(Entity.nid(component.publicId()));
		} else {
			System.out.println("ERROR: Unknown field value type: " + value.toString());
			return Set.of();
		}
	}

	private void processFieldForProps(Map<String, Object> props, String label, Object value) {
		if (value instanceof Instant instant) {
			props.put(label, DateTimeUtil.format(instant));
		} else if (value instanceof BigDecimal bigDecimal) {
			props.put(label, bigDecimal.toPlainString());
		} else if (value instanceof Integer integerValue ||
				value instanceof Long longValue ||
				value instanceof Float floatValue ||
				value instanceof Double doubleValue ||
				value instanceof String string ||
				value instanceof Boolean booleanValue) {
			props.put(label, value);
		}
	}

	private void collectSemanticRelationshipRows(String originId, String originLabel, String destinationId,
												 String destinationLabel, String constellationId, List<Map<String, Object>> data) {
		if (originId == null || originLabel == null || destinationId == null || destinationLabel == null) {
			System.out.println("break");
		}
		Map<String, Object> row = new HashMap<>();
		row.put("originId", originId);
		row.put("originLabel", originLabel);
		row.put("semanticId", destinationId);
		row.put("semanticLabel", destinationLabel);
		row.put("relLabel", "HAS_SEMANTIC");
		row.put("relType", "Has Semantic");
		row.put("constellationId", constellationId);
		data.add(row);
	}

	private void collectSemanticNodeRows(String id, String label, String constellationId, Map<String, Object> props, List<Map<String, Object>> data) {
		Map<String, Object> row = new HashMap<>();
		row.put("id", id);
		row.put("label", label);
		row.put("constellationId", constellationId);
		row.put("props", props);
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

	private void collectSemanticFieldRelationshipRows(String originId, String originLabel, String destinationId,
													  String destinationLabel, String relLabel, String relType,
													  String constellationId, List<Map<String, Object>> data) {
		Map<String, Object> row = new HashMap<>();
		row.put("semanticId", originId);
		row.put("semanticLabel", originLabel);
		row.put("destinationId", destinationId);
		row.put("destinationLabel", destinationLabel);
		row.put("relLabel", relLabel);
		row.put("relType", relType);
		row.put("constellationId", constellationId);
		data.add(row);
	}

}
