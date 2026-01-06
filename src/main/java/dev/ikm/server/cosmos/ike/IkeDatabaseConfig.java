package dev.ikm.server.cosmos.ike;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

import static dev.ikm.tinkar.common.service.CachingService.clearAll;

@Configuration
@ConfigurationProperties(prefix = "ike.database")
public class IkeDatabaseConfig {

	private final Logger LOG = LoggerFactory.getLogger(IkeDatabaseConfig.class);

	private File directory;
	private Type type;

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@PostConstruct
	public void start() {
		LOG.info("Database initialization started");

		//Clear tinkar-core caches
		clearAll();
		LOG.info("Clear database cache");

		//Set up the correct database controller by name
		switch (type) {
			case SA, MV -> {
				if (directory.isDirectory() && directory.exists()) {
					ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, directory);
					PrimitiveData.selectControllerByName(type.getName());
				}
			}
			case null, default -> {
				LOG.warn("Unsupported database type: {}", type);
				throw new RuntimeException("Unsupported database type: " + type);
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

	@Bean
	public PrimitiveDataService getPrimitiveDataService() {
		return PrimitiveData.get();
	}

	@Bean
	EntityService getEntityService() {
		return Entity.provider();
	}
}
