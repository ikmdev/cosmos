package dev.ikm.server.cosmos.constellation;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TaskManager {

	private static final ConcurrentMap<UUID, UUID> activeTasks = new ConcurrentHashMap<>();
}
