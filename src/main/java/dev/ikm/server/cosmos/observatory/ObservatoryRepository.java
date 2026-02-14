package dev.ikm.server.cosmos.observatory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ObservatoryRepository {

	private static final Logger LOG = LoggerFactory.getLogger(ObservatoryRepository.class);

	private final ObservatoryDatabaseConfig observatoryDatabaseConfig;

	@Autowired
	public ObservatoryRepository(ObservatoryDatabaseConfig observatoryDatabaseConfig) {
		this.observatoryDatabaseConfig = observatoryDatabaseConfig;
	}

	public void createObservatory(ObservatoryEntity observatoryEntity) {
		observatoryDatabaseConfig.getObservatoryDB().put(observatoryEntity.id(), observatoryEntity);
	}

	public ObservatoryEntity readObservatory(UUID id) {
		return observatoryDatabaseConfig.getObservatoryDB().get(id);
	}

	public List<ObservatoryEntity> readAll() {
		return observatoryDatabaseConfig.getObservatoryDB().values().stream().toList();
	}

	public void updateObservatory(UUID id, ObservatoryEntity observatoryEntity) {
		observatoryDatabaseConfig.getObservatoryDB().put(id, observatoryEntity);
	}

	public void deleteObservatory(UUID id) {
		observatoryDatabaseConfig.getObservatoryDB().remove(id);
	}

}
