package dev.ikm.server.cosmos.scope;

import dev.ikm.server.cosmos.api.coordinate.Language;
import dev.ikm.server.cosmos.api.coordinate.Navigation;
import dev.ikm.server.cosmos.api.coordinate.Stamp;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

@Configuration
@ConfigurationProperties(prefix = "cosmos.scope.database")
public class ScopeDatabaseConfig {

	private static final Logger LOG = LoggerFactory.getLogger(ScopeDatabaseConfig.class);

	private DB database;
	private File directory;
	private String name;
	private ConcurrentMap<UUID, ScopeEntity> scopeDB;

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@PostConstruct
	public void start() {
		if (directory.exists() && directory.isDirectory()) {
			Path dbFile = directory.toPath().resolve(name + ".db");
			database = DBMaker
					.fileDB(dbFile.toFile())
					.fileMmapEnable()
					.make();

			//Create Default Scope
			UUID defaultId = UUID.fromString("312b2c57-6882-452c-bec4-966ed1af04d8");
			ConcurrentMap<UUID, ScopeEntity> dbMap = database.hashMap(name, Serializer.UUID, Serializer.JAVA).createOrOpen();
			if (!dbMap.containsKey(defaultId)) {
				dbMap.put(defaultId, new ScopeEntity(defaultId, Instant.now(), "Default Scope", Stamp.DEV_LATEST, Language.US_ENG_REG, Navigation.INFERRED));
			}

			LOG.info("Scope database initialized");
		} else {
			throw new RuntimeException("Scope database directory does not exist");
		}
	}

	@PreDestroy
	public void shutdown() {
		if (database != null) {
			database.close();
		}
	}

	@Bean
	public ConcurrentMap<UUID, ScopeEntity> getScopeDB() {
		return database
				.hashMap(name, Serializer.UUID, Serializer.JAVA)
				.createOrOpen();
	}
}
