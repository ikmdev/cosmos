package dev.ikm.server.cosmos.ike;

import java.io.Serializable;

public record Facade (
		Id id,
		Type type,
		String name) implements Serializable {
}
