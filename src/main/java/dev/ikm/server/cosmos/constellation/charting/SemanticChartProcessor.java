package dev.ikm.server.cosmos.constellation.charting;

import dev.ikm.server.cosmos.constellation.Step;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIdList;
import dev.ikm.tinkar.common.id.PublicIdSet;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.Component;
import dev.ikm.tinkar.component.Concept;
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
			MATCH (destination:$(row.destinationLabel) {id: row.destinationId, constellationId: row.constellationId})
			MERGE (origin)-[r:$(row.relLabel) {type: row.relType, constellationId: row.constellationId}]->(destination)""";

	private final String semanticNodeQuery = """
			UNWIND $batch AS row
			MERGE (n:$(row.label) {id: row.id, constellationId: row.constellationId})
			SET n += row.props
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
	public void process(ChartingContext chartContext, int batchSize) {
		List<Map<String, Object>> semanticNodeData = new ArrayList<>();

		StampCalculator stampCalculator = chartContext.getChart().stampCalculator();
		LanguageCalculator languageCalculator = chartContext.getChart().languageCalculator();
		Set<Integer> includedModules = chartContext.getChart().includedModules()
				.stream().map(facade -> facade.id().nid()).collect(Collectors.toSet());
		Set<Integer> excludedModules = chartContext.getChart().excludedModules()
				.stream().map(facade -> facade.id().nid()).collect(Collectors.toSet());

		chartContext.getScopedConcepts().forEach((facade, descendants) -> {
			descendants.forEach(nid -> {

				PrimitiveData.get().forEachSemanticNidForComponent(nid, semanticNid -> {
					Latest<EntityVersion> latest = stampCalculator.latest(semanticNid);
					if (latest.isPresent()) {
						SemanticEntityVersion semanticEntityVersion = (SemanticEntityVersion) latest.get();
						int stampModuleNid = semanticEntityVersion.moduleNid();

						if (includedModules.contains(stampModuleNid) &&
								!excludedModules.contains(stampModuleNid) &&
								!excludedPatternNids().contains(semanticEntityVersion.pattern().nid())) {

							//This Semantic is within the allowed modules to be charted
							String originNid = String.valueOf(semanticEntityVersion.nid());
							int patternNid = semanticEntityVersion.pattern().nid();
							Latest<PatternEntityVersion> latestPattern = stampCalculator.latest(patternNid);
							if (latestPattern.isPresent()) {
								Map<String, Object> props = new HashMap<>();
								PatternEntityVersion patternEntityVersion = latestPattern.get();

								//Get label for Semantic Node
								String originLabel = languageCalculator.getDescriptionTextOrNid(patternEntityVersion.semanticMeaningNid()).replaceAll("[^a-zA-Z0-9]", "");


								semanticEntityVersion.fieldValues().stream()
										.forEach(value -> {
											//Process for new Semantic Node
											int fieldMeaningNid = patternEntityVersion.fieldDefinitions().get(semanticEntityVersion.fieldValues().indexOf(value)).meaningNid();
											String fieldLabel = "ERROR";
											if (semanticEntityVersion.patternNid() == TinkarTermV2.IDENTIFIER_PATTERN.nid()) {
												Latest<Field<Object>> latestField =	stampCalculator.getFieldForSemanticWithMeaning(semanticEntityVersion, TinkarTermV2.IDENTIFIER_SOURCE);
												if (latestField.isPresent()) {
													EntityProxy sourceConcept = (EntityProxy) latestField.get().value();
													fieldLabel = languageCalculator.getDescriptionTextOrNid(sourceConcept);
												}
											} else {
												fieldLabel = languageCalculator.getDescriptionTextOrNid(fieldMeaningNid);
											}

											processFieldForProps(props, fieldLabel, value);
										});

								collectSemanticNodeRows(originNid, originLabel, chartContext.getChart().constellationId().toString(), props, semanticNodeData);
							}
						}

					}
				});
			});
		});

		writeData(semanticNodeQuery, semanticNodeData, chartContext, batchSize);
	}

	private boolean isTinkarIdentifier(SemanticEntityVersion semanticEntityVersion) {

		return false;
	}

//	private Set<Integer> processFieldForRelationships(Object value) {
//		return switch (value) {
//			case PublicIdList publicIdList -> publicIdList.stream()
//					.map(publicIdObj -> {
//						PublicId publicId = (PublicId) publicIdObj;
//						return Entity.nid(publicId);
//					})
//					.collect(Collectors.toSet());
//			case PublicIdSet publicIdSet -> publicIdSet.stream()
//					.map(publicIdObj -> {
//						PublicId publicId = (PublicId) publicIdObj;
//						return publicId.asUuidList().stream().toList();
//					})
//					.collect(Collectors.toSet());
//			case IntIdList intIdList -> intIdList
//					.map(intIdValue -> {
//						Entity<? extends EntityVersion> entity = Entity.getFast(intIdValue);
//						return entity.publicId().asUuidList().stream().toList();
//					})
//					.toList();
//			case IntIdSet intIdSet -> intIdSet
//					.map(intIdValue -> {
//						Entity<? extends EntityVersion> entity = Entity.getFast(intIdValue);
//						return entity.publicId().asUuidList().stream().toList();
//					})
//					.toSet();
//			case Component component -> {
//				component.publicId().asUuidList().stream().toList();
//			}
//			default -> throw new IllegalStateException("Unexpected value for field relationship: " + value);
//		}
//	}

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

	private void collectSemanticNodeRows(String id, String label, String constellationId, Map<String, Object> props, List<Map<String, Object>> data) {
		Map<String, Object> row = new HashMap<>();
		row.put("id", id);
		row.put("label", label);
		row.put("constellationId", constellationId);
		row.put("props", props);
		data.add(row);
	}

}
