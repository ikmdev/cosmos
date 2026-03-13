package dev.ikm.server.cosmos.database;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.ikm.server.cosmos.constellation.ConstellationEntity;
import dev.ikm.server.cosmos.observatory.ObservatoryEntity;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Configuration
public class CosmosDatabaseConfig {

	private static final Logger LOG = LoggerFactory.getLogger(CosmosDatabaseConfig.class);
	public static final UUID DEFAULT_OBSERVATORY_ID = UUID.fromString("312b2c57-6882-452c-bec4-966ed1af04d8");

	private ConcurrentMap<UUID, ObservatoryEntity> observatoryDB;
	private ConcurrentMap<UUID, ConstellationEntity> constellationDB;

	private DB database;
	private final String dataDiredctoryName = "data";
	private final String databaseFileName = "cosmos.db";
	private final String constellationMapName = "constellation";
	private final String observatoryMapName = "observatory";

	@Value("${cosmos.directory}")
	private File directory;

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	@PostConstruct
	public void start() {
		LOG.info("Database initialization started");
		Path dbFile = directory.toPath().resolve(dataDiredctoryName);;

		if (dbFile.toFile().exists() && dbFile.toFile().isDirectory()) {
			database = DBMaker
					.fileDB(dbFile.resolve(databaseFileName).toFile())
					.fileMmapEnable()
					.closeOnJvmShutdown()
					.make();

			observatoryDB = database.hashMap(observatoryMapName, Serializer.UUID, Serializer.JAVA).createOrOpen();
			constellationDB = database.hashMap(constellationMapName, Serializer.UUID, Serializer.JAVA).createOrOpen();
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
		return observatoryDB;
	}

	@Bean
	public ConcurrentMap<UUID, ConstellationEntity> getConstellationDB() {
		return constellationDB;
	}
}
