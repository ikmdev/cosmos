package dev.ikm.server.cosmos.api.semantic;

import dev.ikm.server.cosmos.api.semantic.field.FieldTransformationService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SemanticService {

	private final IkeRepository ikeRepository;
	private final FieldTransformationService fieldTransformationService;

	@Autowired
	public SemanticService(IkeRepository ikeRepository, FieldTransformationService fieldTransformationService) {
		this.ikeRepository = ikeRepository;
		this.fieldTransformationService = fieldTransformationService;
	}

	public SemanticChronologyDTO retrieveSemanticWithAllVersions(UUID id) {
		Optional<SemanticEntity<? extends SemanticEntityVersion>> optionalSemanticEntity = ikeRepository.findSemanticById(id);
		if (optionalSemanticEntity.isPresent()) {
			SemanticEntity<? extends SemanticEntityVersion> semanticEntity = optionalSemanticEntity.get();
			return new SemanticChronologyDTO(
					ikeRepository.getIds(id),
					ikeRepository.getIds(semanticEntity.pattern()),
					ikeRepository.getIds(semanticEntity.referencedComponent()),
					null,
					semanticEntity.versions().stream()
							.map(semanticEntityVersion ->
									new SemanticVersionDTO(
											ikeRepository.getIds(semanticEntityVersion.stamp()),
											semanticEntityVersion.fieldValues().stream()
													.map(fieldTransformationService::transformField)
													.toList()
									))
							.toList());
		} else {
			return new SemanticChronologyDTO(
					ikeRepository.getIds(id),
					null,
					null,
					null,
					Collections.emptyList());
		}
	}

	public SemanticChronologyDTO retrieveSemanticWithLatestVersion(UUID id) {
		Latest<SemanticEntityVersion> latestSemanticEntityVersion = ikeRepository.findLatestSemanticById(id);
		if (latestSemanticEntityVersion.isPresent()) {
			SemanticEntityVersion semanticEntityVersion = latestSemanticEntityVersion.get();
			return new SemanticChronologyDTO(
					ikeRepository.getIds(id),
					ikeRepository.getIds(semanticEntityVersion.pattern()),
					ikeRepository.getIds(semanticEntityVersion.referencedComponent()),
					new SemanticVersionDTO(
							ikeRepository.getIds(semanticEntityVersion.stamp()),
							semanticEntityVersion.fieldValues().stream()
									.map(fieldTransformationService::transformField)
									.toList()),
					null);
		} else {
			return new SemanticChronologyDTO(
					ikeRepository.getIds(id),
					null,
					null,
					null,
					null);
		}
	}

	public List<List<UUID>> retrieveSemantics(UUID id) {
		return ikeRepository.findAssociatedSemanticIds(id);
	}

	public List<UUID> calculateUSDialect(UUID id) {
		return ikeRepository.findUSDialect(id);
	}

	public List<UUID> calculateGBDialect(UUID id) {
		return ikeRepository.findGBDialect(id);
	}
}
