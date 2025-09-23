package dev.ikm.server.rest.semantic.field;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.ikm.server.rest.concept.ConceptChronologyDTO;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VertexFieldView(
		UUID uuid,
		int index,
		ConceptChronologyDTO concept,
		List<PropertyFieldView> properties) {
}
