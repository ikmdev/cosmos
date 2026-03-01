package dev.ikm.server.cosmos.constellation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

	public List<ConstellationEntity> readAll(UUID observatoryId) {
		return constellationDatabaseConfig.getConstellationDB().values().stream()
				.filter(constellationEntity -> constellationEntity.observatoryId().equals(observatoryId))
				.toList();
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

	public void updatePhase(UUID id, Phase phase) {
		ConstellationEntity constellationEntity = readConstellation(id);
		constellationDatabaseConfig.getConstellationDB().put(id, constellationEntity.with(phase));
	}

	public void updateConceptCount(UUID id, long count) {
		ConstellationEntity constellationEntity = readConstellation(id);
		constellationDatabaseConfig.getConstellationDB().put(id, constellationEntity.with(count, 0, 0));
	}

	public void updateSemanticCount(UUID id, long count) {
		ConstellationEntity constellationEntity = readConstellation(id);
		constellationDatabaseConfig.getConstellationDB().put(id, constellationEntity.with(0, count, 0));
	}

	public void updatePatternCount(UUID id, long count) {
		ConstellationEntity constellationEntity = readConstellation(id);
		constellationDatabaseConfig.getConstellationDB().put(id, constellationEntity.with(0, 0, count));
	}

	public void updateCompleted(UUID id, Instant instant) {
		ConstellationEntity constellationEntity = readConstellation(id);
		constellationDatabaseConfig.getConstellationDB().put(id, constellationEntity.with(instant));
	}
}
