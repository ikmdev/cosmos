package dev.ikm.server.cosmos.database;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static dev.ikm.tinkar.common.service.CachingService.clearAll;
import static dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator.LOG;

@Service
public class DatabaseService {

	private final Logger log = LoggerFactory.getLogger(DatabaseService.class);

	private final DatabaseConfig databaseConfig;

	public DatabaseService(DatabaseConfig databaseConfig) {
		this.databaseConfig = databaseConfig;
	}

	@PostConstruct
	public void init() {
		LOG.info("Database initialization started");

		//Clear tinkar-core caches
		clearAll();
		LOG.info("Clear database cache");

		//Setup correct database controller by name
		switch (databaseConfig.getType()) {
			case SA, MV -> {
				if (databaseConfig.getDirectory().isDirectory() && databaseConfig.getDirectory().exists()) {
					ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, databaseConfig.getDirectory());
					PrimitiveData.selectControllerByName(databaseConfig.getType().getName());
				}
			}
			case null, default -> {
				LOG.warn("Unsupported database type: {}", databaseConfig.getType());
				throw new RuntimeException("Unsupported database type: " + databaseConfig.getType());
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

	@PreDestroy
	public void shutdown() {
		LOG.info("Database shutdown started");
		PrimitiveData.stop();
		LOG.info("Database shutdown completed");
	}
}
