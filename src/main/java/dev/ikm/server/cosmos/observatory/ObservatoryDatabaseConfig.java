package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.calculator.LanguageCoordinate;
import dev.ikm.server.cosmos.calculator.NavigationCoordinate;
import dev.ikm.server.cosmos.calculator.StampCoordinate;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

@Configuration
@ConfigurationProperties(prefix = "cosmos.observatory.database")
public class ObservatoryDatabaseConfig {

	private static final Logger LOG = LoggerFactory.getLogger(ObservatoryDatabaseConfig.class);
	public static final UUID DEFAULT_OBSERVATORY_ID = UUID.fromString("312b2c57-6882-452c-bec4-966ed1af04d8");

	private DB database;
	private File directory;
	private String name;
	private ConcurrentMap<UUID, ObservatoryEntity> observatoryDB;

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

			//Create Default Observatory
			ConcurrentMap<UUID, ObservatoryEntity> dbMap = database.hashMap(name, Serializer.UUID, Serializer.JAVA).createOrOpen();
			if (!dbMap.containsKey(DEFAULT_OBSERVATORY_ID)) {
				dbMap.put(DEFAULT_OBSERVATORY_ID, new ObservatoryEntity(
						DEFAULT_OBSERVATORY_ID,
						Instant.now(),
						"Default Observatory",
						StampCoordinate.DEV_LATEST,
						LanguageCoordinate.US_ENG_REG,
						NavigationCoordinate.INFERRED,
						List.of(),
						List.of(),
						List.of()));
			}

			LOG.info("Observatory database initialized");
		} else {
			throw new RuntimeException("Observatory database directory does not exist");
		}
	}

	@PreDestroy
	public void shutdown() {
		if (database != null) {
			database.close();
		}
	}

	@Bean
	public ConcurrentMap<UUID, ObservatoryEntity> getObservatoryDB() {
		return database
				.hashMap(name, Serializer.UUID, Serializer.JAVA)
				.createOrOpen();
	}
}
