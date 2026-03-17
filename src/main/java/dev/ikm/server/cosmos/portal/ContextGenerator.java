package dev.ikm.server.cosmos.portal;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.tinkar.entity.SemanticEntityVersion;

@Component
public class ContextGenerator {

    private final CalculatorService calculatorService;

    public ContextGenerator(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    private final String contextTemplate = """
            ---
            ### Concept %d: %s
            **Synonyms:** %s
            **Definition:** %s

            **Taxonomic Hierarchy:**
            %s

            **Defining Attributes (Axioms):**
            %s

            **Advanced Semantic Data:**
            %s
            ---
            """;

    public String generate(List<Integer> nids) {
        AtomicInteger count = new AtomicInteger(1);
        return "Here are the relevant clinical concepts retrieved from the Tinkar knowledge base:\n\n" +
                nids.stream()
                        .map(nid -> contextTemplate.formatted(
                                count.getAndIncrement(),
                                fqnContext(nid),
                                synContext(nid),
                                defContext(nid),
                                hierarchyContext(nid),
                                axiomContext(nid),
                                semanticDataContext(nid)))
                        .collect(Collectors.joining("\n"));
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
        final String hierarchyTemplate = "* IS_A: %s\n";
        StringBuilder hierarchyBuilder = new StringBuilder();
        calculatorService.calculateParents(nid)
                .forEach(parentFacades -> {
                    
                });

        return hierarchyBuilder.toString();
    }

    private String axiomContext(int nid) {
        final String axiomTemplate = "* %s: %s\n";
        StringBuilder axiomBuilder = new StringBuilder();

        return axiomBuilder.toString();
    }

    private String semanticDataContext(int nid) {
        final String semanticDataTemplate = "* **%s:** %s";
        StringBuilder semanticDataBuilder = new StringBuilder();
        return semanticDataBuilder.toString();
    }
}
