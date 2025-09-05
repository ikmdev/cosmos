package dev.ikm.server.database.boundary;

import dev.ikm.tinkar.common.service.PrimitiveData;
import io.quarkus.runtime.ShutdownEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DatabaseShutdown {

	Logger LOG = LoggerFactory.getLogger(DatabaseShutdown.class);

	public void onStop(@Observes ShutdownEvent shutdownEvent) {
		PrimitiveData.stop();
		LOG.info("Database shutdown complete.");
	}
}
