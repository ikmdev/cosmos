package dev.ikm.server.cosmos.api.stamp;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StampChronology(
		List<UUID> publicId,
		StampVersion latestVersion,
		List<StampVersion> versions) {
}
