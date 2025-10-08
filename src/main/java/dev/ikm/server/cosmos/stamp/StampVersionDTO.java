package dev.ikm.server.cosmos.stamp;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StampVersionDTO(
		List<UUID> statusPublicId,
		long time,
		String formattedTime,
		List<UUID> authorPublicId,
		List<UUID> modulePublicId,
		List<UUID> pathPublicId) {
}
