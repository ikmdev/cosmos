package dev.ikm.server.rest.concept;

import dev.ikm.server.database.IkeRepository;
import dev.ikm.server.database.ViewContext;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConceptService {

	private final IkeRepository ikeRepository;
	private final ViewContext viewContext;

	@Autowired
	public ConceptService(IkeRepository ikeRepository, ViewContext viewContext) {
		this.ikeRepository = ikeRepository;
		this.viewContext = viewContext;
	}

	public ConceptChronologyDTO retrieveConcept(UUID uuid) {
		PublicId conceptPublicId = PublicIds.of(uuid);
		Optional<ConceptEntity<? extends ConceptEntityVersion>> optionalConceptEntity = ikeRepository.findConceptById(conceptPublicId);
		Latest<ConceptEntityVersion> latestConceptEntity = ikeRepository.findLatestConceptById(conceptPublicId, viewContext.getStampContext());
		int latestIndex = findLatestVersionIndex(optionalConceptEntity, latestConceptEntity);
		List<ConceptVersionDTO> versions = buildConceptVersionList(optionalConceptEntity);
		return new ConceptChronologyDTO(
				conceptPublicId.asUuidList().stream().toList(),
				latestIndex,
				versions
		);
	}

	private int findLatestVersionIndex(Optional<ConceptEntity<? extends ConceptEntityVersion>> optionalConceptEntity,
									   Latest<ConceptEntityVersion> latestConceptEntity) {
		if (optionalConceptEntity.isEmpty() || latestConceptEntity.isAbsent()) {
			return -1;
		}
		ConceptEntityVersion latestConceptEntityVersion = latestConceptEntity.get();
		if (optionalConceptEntity.get().versions().contains(latestConceptEntityVersion)) {
			return optionalConceptEntity.get().versions().indexOf(latestConceptEntityVersion);
		}
		return -1;
	}

	private List<ConceptVersionDTO> buildConceptVersionList(Optional<ConceptEntity<? extends ConceptEntityVersion>> optionalConceptEntity) {
		return optionalConceptEntity.map(conceptEntity -> conceptEntity.versions().stream()
						.map(conceptEntityVersion -> new ConceptVersionDTO(
								conceptEntityVersion.stamp().publicId().asUuidList().stream().toList()))
						.toList())
				.orElse(Collections.emptyList());
	}
}
