package dev.ikm.server.rest.semantic;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.ikm.server.rest.concept.ConceptChronologyDTO;
import dev.ikm.server.rest.pattern.PatternChronologyView;
import dev.ikm.server.rest.stamp.StampChronologyView;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SemanticChronologyView(
		List<UUID> publicId,
		PatternChronologyView pattern,
		ConceptChronologyDTO referencedConcept,
		PatternChronologyView referencedPattern,
		SemanticChronologyView referencedSemantic,
		StampChronologyView referencedStamp,
		SemanticVersionView latestVersion,
		List<SemanticVersionView> history) {
}
