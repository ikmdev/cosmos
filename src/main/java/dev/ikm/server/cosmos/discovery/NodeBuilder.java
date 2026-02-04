package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.tinkar.component.ChronologyService;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.FieldDefinitionForEntity;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NodeBuilder {

	private final CalculatorService calculatorService;
	private final ChronologyService chronologyService;

	@Autowired
	public NodeBuilder(CalculatorService calculatorService, ChronologyService chronologyService) {
		this.calculatorService = calculatorService;
		this.chronologyService = chronologyService;
	}

	// chronology:version:field
	private static final String SEPARATOR = ":";

	public String generateChronologyId(int chronologyNid) {
		return Integer.toString(chronologyNid);
	}

	public String generateVersionId(int chronologyNid, int stampNid) {
		return chronologyNid + SEPARATOR + stampNid;
	}

	public String generateFieldId(int chronologyNid, int stampNid, int fieldIndex) {
		return chronologyNid + SEPARATOR + stampNid + SEPARATOR + fieldIndex;
	}

	public int parseChronologyNid(String nodeId) {
		return Integer.parseInt(nodeId.split(SEPARATOR)[0]);
	}

	public int parseVersionSTAMPNid(String nodeId) {
		return Integer.parseInt(nodeId.split(SEPARATOR)[1]);
	}

	public int parseFieldIndex(String nodeId) {
		return Integer.parseInt(nodeId.split(SEPARATOR)[2]);
	}

	public boolean isChronology(String nodeId) {
		return countSeparator(nodeId) == 0;
	}

	public boolean isVersion(String nodeId) {
		return countSeparator(nodeId) == 1;
	}

	public boolean isField(String nodeId) {
		return countSeparator(nodeId) == 2;
	}


	private long countSeparator(String nodeId) {
		return nodeId.chars()
				.filter(ch -> ch == SEPARATOR.charAt(0))
				.count();
	}

	private Node makeNode(String id, String label, NodeType nodeType, List<String> values) {
		return new Node(
				id,
				nodeType.displayName(),
				label,
				nodeType.groupId(),
				nodeType.size(),
				values);
	}

	public Node buildConceptChronology(ConceptEntity conceptEntity) {
		return makeNode(generateChronologyId(conceptEntity.nid()), conceptEntity.publicId().idString(), NodeType.CONCEPT_CHRONOLOGY, List.of());
	}

	public Node buildConceptVersion(ConceptEntityVersion conceptEntityVersion) {
		List<String> values = new ArrayList<>();
		conceptEntityVersion.publicId().asUuidList().forEach(uuid -> values.add("UUID: " + uuid.toString()));
		String id = generateVersionId(conceptEntityVersion.chronology().nid(), conceptEntityVersion.stamp().nid());
		return makeNode(id, conceptEntityVersion.chronology().idString(), NodeType.CONCEPT_VERSION, values);
	}

	public Node buildSemanticChronology(SemanticEntity semanticEntity) {
		List<String> values = new ArrayList<>();
		values.add("Reference: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(semanticEntity.referencedComponentNid()));
		values.add("Pattern: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(semanticEntity.patternNid()));

		return makeNode(generateChronologyId(semanticEntity.nid()), semanticEntity.idString(), NodeType.SEMANTIC_CHRONOLOGY, values);
	}

	public Node buildSemanticVersion(SemanticEntityVersion semanticEntityVersion) {
		List<String> values = new ArrayList<>();

		for (int i = 0; i < semanticEntityVersion.fieldValues().size(); i++) {
			Object object = semanticEntityVersion.fieldValues().get(i);
			if (object instanceof EntityFacade entityFacade) {
				values.add("Entity: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entityFacade));
			} else {
				switch (semanticEntityVersion.fieldDataType(i)) {
					case LONG -> values.add("Long: " + semanticEntityVersion.fieldAsLong(i));
					case FLOAT -> values.add("Float: " + semanticEntityVersion.fieldAsFloat(i));
					case BOOLEAN -> values.add("Boolean: " + semanticEntityVersion.fieldAsBoolean(i));
					case INTEGER -> values.add("Integer: " + semanticEntityVersion.fieldAsInt(i));
					case STRING -> values.add("String: " + semanticEntityVersion.fieldAsString(i));
					case DECIMAL -> values.add("Decimal: " + semanticEntityVersion.fieldAsDouble(i));
					case COMPONENT_ID_LIST -> {

						values.add("Component: " + object);
					}
					case COMPONENT_ID_SET -> {
						values.add("Component: " + object);
					}
					case DITREE -> values.add("DITree: " + object);
					default ->
							throw new RuntimeException("Unsupported data type:" + semanticEntityVersion.fieldDataType(i));
				}
				;
			}
		}
		String id = generateVersionId(semanticEntityVersion.chronology().nid(), semanticEntityVersion.stamp().nid());
		return makeNode(id, "", NodeType.SEMANTIC_VERSION, values);
	}

	public Node buildPatternChronology(PatternEntity patternEntity) {
		return makeNode(generateChronologyId(patternEntity.nid()), patternEntity.idString(), NodeType.PATTERN_CHRONOLOGY, List.of());
	}

	public Node buildPatternVersion(PatternEntityVersion patternEntityVersion) {
		List<String> values = new ArrayList<>();

		int meaningNid = patternEntityVersion.semanticMeaningNid();
		Entity.get(meaningNid).ifPresent(entity -> values.add("Meaning: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entity)));

		int purposeNid = patternEntityVersion.semanticPurposeNid();
		Entity.get(purposeNid).ifPresent(entity -> values.add("Purpose: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entity)));

		String id = generateVersionId(patternEntityVersion.chronology().nid(), patternEntityVersion.stamp().nid());
		return makeNode(id, "", NodeType.PATTERN_VERSION, values);
	}

	public Node buildPatternVersionField(FieldDefinitionForEntity fieldDefinitionForEntity) {
		List<String> values = new ArrayList<>();
		FieldDefinitionRecord fieldDefinitionRecord = (FieldDefinitionRecord) fieldDefinitionForEntity;

		int meaningNid = fieldDefinitionRecord.meaningNid();
		Entity.get(meaningNid).ifPresent(entity -> values.add("Meaning: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entity)));

		int purposeNid = fieldDefinitionRecord.purposeNid();
		Entity.get(purposeNid).ifPresent(entity -> values.add("Purpose: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entity)));

		int dataTypeNid = fieldDefinitionRecord.dataTypeNid();
		Entity.get(dataTypeNid).ifPresent(entity -> values.add("Data Type: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entity)));

		int index = fieldDefinitionRecord.indexInPattern();
		values.add("Index: " + index);

		String id = generateFieldId(fieldDefinitionRecord.patternNid(), fieldDefinitionRecord.patternVersionStampNid(), index);
		return makeNode(id, "", NodeType.PATTERN_FIELD, values);
	}

	public Node buildStampChronology(StampEntity stampEntity) {
		return makeNode(generateChronologyId(stampEntity.nid()), stampEntity.idString(), NodeType.STAMP_CHRONOLOGY, List.of());
	}

	public Node buildStampVersion(StampEntityVersion stampEntityVersion) {
		List<String> values = new ArrayList<>();

		int statusNid = stampEntityVersion.stateNid();
		Entity.get(statusNid).ifPresent(entity -> values.add("Status: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entity)));

		long time = stampEntityVersion.time();
		values.add("Time: " + Long.toString(time));

		int authorNid = stampEntityVersion.authorNid();
		Entity.get(authorNid).ifPresent(entity -> values.add("Author: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entity)));

		int moduleNid = stampEntityVersion.moduleNid();
		Entity.get(moduleNid).ifPresent(entity -> values.add("Module: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entity)));

		int pathNid = stampEntityVersion.pathNid();
		Entity.get(pathNid).ifPresent(entity -> values.add("Path: " + calculatorService.getLanguageCalculator().getDescriptionTextOrNid(entity)));

		String id = generateVersionId(stampEntityVersion.chronology().nid(), stampEntityVersion.stampNid()); //TODO: may need to handle nid of a nid??
		return makeNode(id, "", NodeType.STAMP_VERSION, values);
	}
}
