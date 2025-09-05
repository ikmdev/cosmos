package dev.ikm.server.database.entity;

import java.util.List;
import java.util.UUID;

public record PublicIdView(List<UUID> uuids) {
}
