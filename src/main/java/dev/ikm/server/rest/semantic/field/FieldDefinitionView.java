package dev.ikm.server.rest.semantic.field;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.ikm.server.rest.concept.ConceptChronologyDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldDefinitionView(
		ConceptChronologyDTO dataType,
		ConceptChronologyDTO meaning,
		ConceptChronologyDTO purpose,
		int index) {
}
