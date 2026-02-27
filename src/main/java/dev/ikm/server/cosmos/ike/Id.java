package dev.ikm.server.cosmos.ike;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record Id(
		int nid,
		List<UUID> uuids) implements Serializable {
}
