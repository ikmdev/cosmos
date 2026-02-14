
package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.api.coordinate.Coordinate;
import dev.ikm.server.cosmos.api.coordinate.CoordinateService;
import dev.ikm.server.cosmos.api.coordinate.Language;
import dev.ikm.server.cosmos.api.coordinate.Navigation;
import dev.ikm.server.cosmos.api.coordinate.Stamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ObservatoryService {

	private final ObservatoryRepository observatoryRepository;
	private final CoordinateService coordinateService;

	@Autowired
	public ObservatoryService(ObservatoryRepository observatoryRepository, CoordinateService coordinateService) {
		this.observatoryRepository = observatoryRepository;
		this.coordinateService = coordinateService;
	}

	public Observatory saveNewObservatory(String name,
							  List<UUID> stampId,
							  List<UUID> languageId,
							  List<UUID> navigationId) {
		UUID id = UUID.randomUUID();
		Stamp stamp = coordinateService.stampCoordinate(stampId).get();
		Language language = coordinateService.languageCoordinate(languageId).get();
		Navigation navigation = coordinateService.navigationCoordinate(navigationId).get();

		observatoryRepository.createObservatory(new ObservatoryEntity(id, Instant.now(), name, stamp, language, navigation));

		Coordinate stampCoordinate = coordinateService.stampCoordinate(stamp);
		Coordinate languageCoordinate = coordinateService.languageCoordinate(language);
		Coordinate navigationCoordinate = coordinateService.navigationCoordinate(navigation);

		return new Observatory(id, name, stampCoordinate, languageCoordinate, navigationCoordinate);
	}

	public Observatory retrieveObservatory(UUID id) {
		ObservatoryEntity observatoryEntity = observatoryRepository.readObservatory(id);
		Coordinate stampCoordinate = coordinateService.stampCoordinate(observatoryEntity.stamp());
		Coordinate languageCoordinate = coordinateService.languageCoordinate(observatoryEntity.language());
		Coordinate navigationCoordinate = coordinateService.navigationCoordinate(observatoryEntity.navigation());
		return new Observatory(observatoryEntity.id(), observatoryEntity.name(), stampCoordinate, languageCoordinate, navigationCoordinate);
	}

	public List<Observatory> retrieveAllObservatories() {
		return observatoryRepository.readAll().stream()
				.sorted(Comparator.comparing(ObservatoryEntity::modified).reversed())
				.map(observatoryEntity ->
						new Observatory(observatoryEntity.id(),
								observatoryEntity.name(),
								coordinateService.stampCoordinate(observatoryEntity.stamp()),
								coordinateService.languageCoordinate(observatoryEntity.language()),
								coordinateService.navigationCoordinate(observatoryEntity.navigation()))
				)
				.toList();
	}

	public void removeObservatory(UUID id) {
		observatoryRepository.deleteObservatory(id);
	}

	public void updateObservatory(Observatory observatory) {
		observatoryRepository.updateObservatory(observatory.id(), new ObservatoryEntity(
				observatory.id(),
				Instant.now(),
				observatory.name(),
				coordinateService.stampCoordinate(observatory.stampCoordinate().id()).get(),
				coordinateService.languageCoordinate(observatory.languageCoordinate().id()).get(),
				coordinateService.navigationCoordinate(observatory.navigationCoordinate().id()).get()
		));
	}
}
