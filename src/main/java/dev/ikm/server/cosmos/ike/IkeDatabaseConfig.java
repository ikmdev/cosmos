package dev.ikm.server.cosmos.ike;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


@Configuration
public class IkeDatabaseConfig {

	private final Logger LOG = LoggerFactory.getLogger(IkeDatabaseConfig.class);

	private final String ikeDirectoryName = "ike-db";

	@Value("${cosmos.directory}")
	private File directory;

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	@PostConstruct
	public void start() {
		LOG.info("Database initialization started");
		CachingService.clearAll();
		LOG.info("Clear database cache");

		File ikeDirectory = new File(directory, ikeDirectoryName);

		if (ikeDirectory.exists() && ikeDirectory.isDirectory()) {
			ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, ikeDirectory);
			PrimitiveData.selectControllerByName("Open SpinedArrayStore");

			// Log useful JVM information
			LOG.info("JVM Version: {}", System.getProperty("java.version"));
			LOG.info("JVM Name: {}", System.getProperty("java.vm.name"));
			LOG.info(ServiceProperties.jvmUuid());

			// Start database
			PrimitiveData.start();

			LOG.info("Database initialization completed");
		} else {
			throw new RuntimeException("Data directory does not exist: " + directory.getAbsolutePath());
		}
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
