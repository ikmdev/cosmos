package dev.ikm.server.cosmos.api.concept;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConceptVersion(
		List<UUID> stampPublicId) {
}
