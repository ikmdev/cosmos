package dev.ikm.server.rest.stamp;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.ikm.server.rest.concept.ConceptChronologyDTO;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StampVersionView(
		ConceptChronologyDTO status,
		long time,
		String formattedTime,
		List<UUID> authorPublicId,
		List<UUID> modulePublicId,
		List<UUID> pathPublicId) {
}
