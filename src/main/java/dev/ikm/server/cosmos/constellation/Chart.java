package dev.ikm.server.cosmos.constellation;

import dev.ikm.server.cosmos.ike.Facade;

import java.util.List;
import java.util.UUID;

public record Chart(UUID constellationId, UUID observatoryId, List<Facade> scopes) {
}