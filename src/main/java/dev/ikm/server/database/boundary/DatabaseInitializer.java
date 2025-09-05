package dev.ikm.server.database.boundary;

import dev.ikm.server.database.entity.DatabaseType;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class DatabaseInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseInitializer.class);

	@ConfigProperty(name = "database.directory")
	Path databaseDirectory;

	@ConfigProperty(name = "database.type")
	DatabaseType databaseType;

	public void onStart(@Observes StartupEvent event) {
		LOG.info("Database initialization started");

		//Clear tinkar-core caches
		CachingService.clearAll();
		LOG.info("Clear database cache");

		//Setup correct database controller by name
		switch (databaseType) {
			case SA, MV -> {
				if (Files.isDirectory(databaseDirectory) && databaseDirectory.toFile().exists()) {
					ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, databaseDirectory.toFile());
					PrimitiveData.selectControllerByName(databaseType.getName());
				}
			}
			case null, default -> {
				LOG.warn("Unsupported database type: {}", databaseType);
				throw new RuntimeException("Unsupported database type: " + databaseType);
			}
		}

		//Log useful JVM information
		LOG.info("JVM Version: {}", System.getProperty("java.version"));
		LOG.info("JVM Name: {}", System.getProperty("java.vm.name"));
		LOG.info(ServiceProperties.jvmUuid());

		//Start database
		PrimitiveData.start();

		LOG.info("Database initialization completed");
	}
}
