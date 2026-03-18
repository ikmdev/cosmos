package dev.ikm.server.cosmos.portal;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.portal.definition.Definition;
import dev.ikm.server.cosmos.portal.definition.Role;
import dev.ikm.server.cosmos.portal.definition.RoleGroup;
import dev.ikm.server.cosmos.portal.definition.Type;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.terms.TinkarTermV2;

@Component
public class ContextGenerator {

    private final CalculatorService calculatorService;

    public ContextGenerator(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    public String generate(List<Integer> nids) {
        AtomicInteger count = new AtomicInteger(1);
        return "Here are the relevant clinical concepts retrieved from the Tinkar knowledge base:\n\n" +
                nids.stream()
                        .map(nid -> buildIndividualContext(nid, count.getAndIncrement()))
                        .collect(Collectors.joining("\n"));
    }

    public String buildIndividualContext(int nid, int count) {
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
            individualContextBuilder.append(hierarchyContext + "\n\n");
        }
        // Axiom Context
        String axiomContext = axiomContext(nid);
        if (!axiomContext.isEmpty()) {
            individualContextBuilder.append(axiomContext + "\n\n");
        }
        // Semantic Data Context
        String semanticDataContext = semanticDataContext(nid);
        if (!semanticDataContext.isEmpty()) {
            individualContextBuilder.append("**Advanced Semantic Data:**\n");
            individualContextBuilder.append(semanticDataContext + "\n\n");
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
                                                            roleGroup -> roleGroupContext(axiomBuilder, roleGroup, roleGroupNumber.getAndIncrement()));
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
        final String semanticDataTemplate = "* **%s:** %s";
        StringBuilder semanticDataBuilder = new StringBuilder();

        return semanticDataBuilder.toString();
    }
}
