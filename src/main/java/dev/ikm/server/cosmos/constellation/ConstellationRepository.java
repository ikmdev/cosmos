package dev.ikm.server.cosmos.constellation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ConstellationRepository {

	private final ConstellationDatabaseConfig constellationDatabaseConfig;

	@Autowired
	public ConstellationRepository(ConstellationDatabaseConfig constellationDatabaseConfig) {
		this.constellationDatabaseConfig = constellationDatabaseConfig;
	}

	public void  createConstellation(ConstellationEntity constellationEntity) {
		constellationDatabaseConfig.getConstellationDB().put(constellationEntity.id(), constellationEntity);
	}

	public ConstellationEntity readConstellation(UUID id) {
		return constellationDatabaseConfig.getConstellationDB().get(id);
	}

	public List<ConstellationEntity> readAll() {
		return constellationDatabaseConfig.getConstellationDB().values().stream().toList();
	}

	public void updateConstellation(UUID id, ConstellationEntity constellationEntity) {
		constellationDatabaseConfig.getConstellationDB().put(id, constellationEntity);
	}

	public void deleteConstellation(UUID id) {
		constellationDatabaseConfig.getConstellationDB().remove(id);
	}
}
