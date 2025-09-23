package dev.ikm.server.rest.concept;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConceptVersionDTO(
		List<UUID> stampPublicId) {
}
