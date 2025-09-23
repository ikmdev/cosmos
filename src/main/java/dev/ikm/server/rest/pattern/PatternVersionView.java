package dev.ikm.server.rest.pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.ikm.server.rest.semantic.field.FieldDefinitionView;
import dev.ikm.server.rest.concept.ConceptChronologyDTO;
import dev.ikm.server.rest.stamp.StampChronologyView;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatternVersionView(
		StampChronologyView stamp,
		ConceptChronologyDTO meaning,
		ConceptChronologyDTO purpose,
		List<FieldDefinitionView> fieldDefinitions) {
}
