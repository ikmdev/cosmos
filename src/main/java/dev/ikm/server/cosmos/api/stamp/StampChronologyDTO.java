package dev.ikm.server.cosmos.api.stamp;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StampChronologyDTO(
		List<UUID> publicId,
		StampVersionDTO latestVersion,
		List<StampVersionDTO> versions) {
}
