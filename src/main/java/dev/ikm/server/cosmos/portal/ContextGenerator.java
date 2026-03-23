package dev.ikm.server.cosmos.portal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Component;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.logging.RequestLoggingConfig;
import dev.ikm.server.cosmos.portal.definition.Definition;
import dev.ikm.server.cosmos.portal.definition.Role;
import dev.ikm.server.cosmos.portal.definition.RoleGroup;
import dev.ikm.server.cosmos.portal.definition.Type;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIdList;
import dev.ikm.tinkar.common.id.PublicIdSet;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.TinkarTermV2;

@Component
public class ContextGenerator {

    private final RequestLoggingConfig requestLoggingConfig;
    private final CalculatorService calculatorService;
    private final List<Integer> patternFilter;

    public ContextGenerator(CalculatorService calculatorService, RequestLoggingConfig requestLoggingConfig) {
        this.calculatorService = calculatorService;
        this.requestLoggingConfig = requestLoggingConfig;
        this.patternFilter = List.of(TinkarTermV2.DESCRIPTION_PATTERN.nid(),
                TinkarTermV2.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid(),
                TinkarTermV2.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
                TinkarTermV2.STATED_NAVIGATION_PATTERN.nid(),
                TinkarTermV2.INFERRED_NAVIGATION_PATTERN.nid(),
                TinkarTermV2.OWL_AXIOM_SYNTAX_PATTERN.nid());
    }

    public String generate(List<Integer> nids) {
        AtomicInteger count = new AtomicInteger(1);
        return "Here are the relevant clinical concepts retrieved from the Tinkar knowledge base:\n\n" +
                nids.stream()
                        .map(nid -> buildIndividualContext(nid, count.getAndIncrement()))
                        .collect(Collectors.joining("\n"));
    }

    private String buildIndividualContext(int nid, int count) {
        StringBuilder individualContextBuilder = new StringBuilder();
        individualContextBuilder.append("---\n");
        // Fully Qualified Name Context
        String fqn = fqnContext(nid);
        if (!fqn.equals("ERROR: FQN NOT FOUND!")) {
            individualContextBuilder.append("### Concept " + count + ": " + fqn + "\n");
        }
        // Synonym Context
        String syn = synContext(nid);
        if (!syn.equals("ERROR: SYN NOT FOUND!")) {
            individualContextBuilder.append("**Synonyms:** " + syn + "\n\n");
        }
        // Definition Context
        String def = defContext(nid);
        if (!def.equals("ERROR: DEF NOT FOUND!")) {
            individualContextBuilder.append("**Definition:** " + def + "\n\n");
        }
        // Taxonomic Hierarchy Context
        String hierarchyContext = hierarchyContext(nid);
        if (!hierarchyContext.isEmpty()) {
            individualContextBuilder.append("**Taxonomic Hierarchy:**" + "\n");
            individualContextBuilder.append(hierarchyContext + "\n");
        }
        // Axiom Context
        String axiomContext = axiomContext(nid);
        if (!axiomContext.isEmpty()) {
            individualContextBuilder.append(axiomContext + "\n");
        }
        // Semantic Data Context
        String semanticDataContext = semanticDataContext(nid);
        if (!semanticDataContext.isEmpty()) {
            individualContextBuilder.append("**Additional Semantic Data:**\n");
            individualContextBuilder.append(semanticDataContext);
        }
        individualContextBuilder.append("---");
        return individualContextBuilder.toString();
    }

    private String fqnContext(int nid) {
        return calculatorService.calculateFQN(nid);
    }

    private String synContext(int nid) {
        return calculatorService.calculateSYN(nid);
    }

    private String defContext(int nid) {
        return calculatorService.calculateDEF(nid);
    }

    private String hierarchyContext(int nid) {
        StringBuilder hierarchyBuilder = new StringBuilder();
        calculatorService.calculateParents(nid).stream()
                .filter(facade -> facade.id().nid() != TinkarTermV2.INTEGRATED_KNOWLEDGE_MANAGEMENT.nid())
                .forEach(parentFacade -> {
                    String parentText = calculatorService.calculateText(parentFacade.id().nid());
                    hierarchyBuilder.append("* IS-A: " + parentText + "\n");
                });
        return hierarchyBuilder.toString();
    }

    private String axiomContext(int nid) {
        StringBuilder axiomBuilder = new StringBuilder();
        PrimitiveData.get().forEachSemanticNidForComponentOfPattern(nid,
                TinkarTermV2.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid(), semanticNid -> {
                    Latest<EntityVersion> latestEntityVersion = calculatorService.getStampCalculator()
                            .latest(semanticNid);
                    if (latestEntityVersion.isPresent()) {
                        SemanticEntityVersion semanticEntityVersion = (SemanticEntityVersion) latestEntityVersion.get();
                        calculatorService.getStampCalculator().getFieldForSemanticWithMeaning(semanticEntityVersion,
                                TinkarTermV2.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS).ifPresent(field -> {
                                    final DiTreeEntity diTreeEntity = (DiTreeEntity) field.value();
                                    LogicalDefinitionParser ldp = new LogicalDefinitionParser(diTreeEntity);
                                    Definition definition = ldp.parse();
                                    definition.sets()
                                            .forEach(clause -> {
                                                if (clause.roles().size() != 0 || clause.roleGroups().size() != 0) {
                                                    if (clause.element() == Type.NECESSARY_SET) {
                                                        axiomBuilder.append(
                                                                "**Necessary Conditions (Defining Attributes):**\n");
                                                    } else {
                                                        axiomBuilder.append(
                                                                "**Sufficient Conditions (Defining Attributes):**\n");
                                                    }
                                                    // Generate Role Context
                                                    clause.roles()
                                                            .forEach(role -> roleContext(axiomBuilder, role, false));
                                                    // Generate Role Groups then Role Context
                                                    AtomicInteger roleGroupNumber = new AtomicInteger(1);
                                                    clause.roleGroups().forEach(
                                                            roleGroup -> roleGroupContext(axiomBuilder, roleGroup,
                                                                    roleGroupNumber.getAndIncrement()));
                                                }
                                            });
                                });
                    }
                });
        return axiomBuilder.toString();
    }

    private void roleContext(StringBuilder stringBuilder, Role role, boolean indent) {
        String predicateName = calculatorService.calculateText(role.predicate().nid());
        String objectName = calculatorService.calculateText(role.reference().concept().nid());
        if (indent) {
            stringBuilder.append(
                    "\t* " + predicateName + " - " + objectName + "\n");
        } else {
            stringBuilder.append(
                    "* " + predicateName + " - " + objectName + "\n");
        }
    }

    private void roleGroupContext(StringBuilder stringBuilder, RoleGroup roleGroup, int roleGroupNumber) {
        stringBuilder.append("* Role Group " + roleGroupNumber + ":\n");
        roleGroup.roles().forEach(role -> roleContext(stringBuilder, role, true));
    }

    private String semanticDataContext(int nid) {
        StringBuilder semanticDataBuilder = new StringBuilder();
        PrimitiveData.get().forEachSemanticNidForComponent(nid, semanticNid -> {
            Latest<SemanticEntityVersion> latestSemanticVersion = calculatorService.getStampCalculator()
                    .latest(semanticNid);
            if (latestSemanticVersion.isPresent()
                    && !patternFilter.contains(latestSemanticVersion.get().patternNid())) {
                SemanticEntityVersion semanticEntityVersion = latestSemanticVersion.get();
                Latest<PatternEntityVersion> latestPattern = calculatorService.getStampCalculator()
                        .latest(semanticEntityVersion.patternNid());
                if (latestPattern.isPresent()) {
                    PatternEntityVersion patternEntityVersion = latestPattern.get();
                    String meaning = calculatorService.getLanguageCalculator()
                            .getDescriptionTextOrNid(patternEntityVersion.semanticMeaningNid());
                    semanticDataBuilder.append("* [Semantic: " + meaning + "]\n");
                    individualSemanticDataContext(semanticDataBuilder, semanticEntityVersion, patternEntityVersion, 0);
                }
            }
        });

        return semanticDataBuilder.toString();
    }

    private void individualSemanticDataContext(StringBuilder semanticDataBuilder,
            SemanticEntityVersion semanticEntityVersion,
            PatternEntityVersion patternEntityVersion, int depth) {
        for (int idx = 0; idx < semanticEntityVersion.fieldValues().size(); idx++) {
            Object value = semanticEntityVersion.fieldValues().get(idx);
            // Process for new Semantic Node
            int fieldMeaningNid = patternEntityVersion.fieldDefinitions()
                    .get(semanticEntityVersion.fieldValues().indexOf(value)).meaningNid();
            String fieldName = calculatorService.getLanguageCalculator().getDescriptionTextOrNid(fieldMeaningNid);
            String fieldValue = processSemanticField(value);
            for (int t = 0; t <= depth; t++) {
                semanticDataBuilder.append("\t");
            }
            semanticDataBuilder.append("* " + fieldName + ": " + fieldValue + "\n");
        }

        // Recursive call to other Semantics
        PrimitiveData.get().forEachSemanticNidForComponent(semanticEntityVersion.nid(), nextSemantic -> {
            Latest<SemanticEntityVersion> nextSemanticlatest = calculatorService.getStampCalculator()
                    .latest(nextSemantic);

            if (nextSemanticlatest.isPresent()) {
                SemanticEntityVersion nextSemanticEntityVersion = nextSemanticlatest.get();
                Latest<PatternEntityVersion> nextLatestPattern = calculatorService.getStampCalculator()
                        .latest(nextSemanticEntityVersion.patternNid());
                if (nextLatestPattern.isPresent()) {
                    PatternEntityVersion nextPatternEntityVersion = nextLatestPattern.get();
                    String nextMeaning = calculatorService.getLanguageCalculator()
                            .getDescriptionTextOrNid(nextPatternEntityVersion.semanticMeaningNid());
                    for (int t = 0; t <= depth; t++) {
                        semanticDataBuilder.append("\t");
                    }
                    semanticDataBuilder.append("* -> [Semantic: " + nextMeaning + "]\n");
                    individualSemanticDataContext(semanticDataBuilder, nextSemanticEntityVersion,
                            nextPatternEntityVersion, depth + 1);
                }
            }
        });
    }

    private String processSemanticField(Object value) {
        final StringBuilder fieldBuilder = new StringBuilder();
        return switch (value) {
            case PublicIdList publicIdList -> {
                for (PublicId publicId : publicIdList.toIdArray()) {
                    String conceptName = calculatorService.calculateText(publicId);
                    fieldBuilder.append(conceptName + ", ");
                }
                yield fieldBuilder.toString().substring(0, fieldBuilder.toString().length() - 2);
            }
            case PublicIdSet publicIdSet -> {
                for (PublicId publicId : publicIdSet.toIdArray()) {
                    String conceptName = calculatorService.calculateText(publicId);
                    fieldBuilder.append(conceptName + ", ");
                }
                yield fieldBuilder.toString().substring(0, fieldBuilder.toString().length() - 2);
            }
            case IntIdList intIdList -> {
                int[] nids = intIdList.toArray();
                for (int i = 0; i < nids.length; i++) {
                    String conceptName = calculatorService.calculateText(nids[i]);
                    fieldBuilder.append(conceptName + ", ");
                }
                if (intIdList.size() != 0) {
                    yield fieldBuilder.toString().substring(0, fieldBuilder.toString().length() - 2);
                }
                yield "";
            }
            case IntIdSet intIdSet -> {
                int[] nids = intIdSet.toArray();
                for (int i = 0; i < nids.length; i++) {
                    String conceptName = calculatorService.calculateText(nids[i]);
                    fieldBuilder.append(conceptName + ", ");
                }
                if (intIdSet.size() != 0) {
                    yield fieldBuilder.toString().substring(0, fieldBuilder.toString().length() - 2);
                }
                yield "";
            }
            case dev.ikm.tinkar.component.Component component -> calculatorService.calculateText(component.publicId());
            case Instant instant -> DateTimeUtil.format(instant);
            case BigDecimal bigDecimal -> bigDecimal.toPlainString();
            case Integer intValue -> String.valueOf(intValue);
            case Long longValue -> String.valueOf(longValue);
            case Float floatValue -> String.valueOf(floatValue);
            case Double doubleValue -> String.valueOf(doubleValue);
            case String stringValue -> stringValue;
            case Boolean booleanValue -> String.valueOf(booleanValue);
            case null, default -> throw new RuntimeException("Field Object value not supported.");
        };
    }
}
