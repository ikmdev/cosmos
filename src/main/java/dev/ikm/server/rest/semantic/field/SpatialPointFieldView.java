package dev.ikm.server.rest.semantic.field;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpatialPointFieldView(
		float x,
		float y,
		float z) {
}
