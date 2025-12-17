package dev.ikm.server.cosmos.api.concept;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
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
	private final CalculatorService calculatorService;

	@Autowired
	public ConceptService(IkeRepository ikeRepository, CalculatorService calculatorService) {
		this.ikeRepository = ikeRepository;
		this.calculatorService = calculatorService;
	}

	public ConceptChronologyDTO retrieveConceptWithAllVersions(UUID id) {
		Optional<ConceptEntity<? extends ConceptEntityVersion>> optionalConceptEntity = ikeRepository.findConceptById(id);
		return new ConceptChronologyDTO(
				ikeRepository.getIds(id),
				null,
				optionalConceptEntity.map(conceptEntity -> conceptEntity.versions().stream()
								.map(conceptEntityVersion ->
										new ConceptVersionDTO(conceptEntityVersion.stamp().publicId().asUuidList().toList()))
								.toList())
						.orElse(Collections.emptyList())
		);
	}

	public ConceptChronologyDTO retrieveConceptWithLatestVersion(UUID id) {
		Latest<ConceptEntityVersion> latestConceptEntityVersion = ikeRepository.findLatestConceptById(id);
		if (latestConceptEntityVersion.isPresent()) {
			ConceptEntityVersion conceptEntityVersion = latestConceptEntityVersion.get();
			return new ConceptChronologyDTO(
					ikeRepository.getIds(id),
					new ConceptVersionDTO(ikeRepository.getIds(conceptEntityVersion.stamp())),
					null);
		} else {
			return new ConceptChronologyDTO(
					ikeRepository.getIds(id),
					null,
					null);
		}
	}

	public List<List<UUID>> retrieveSemantics(UUID id) {
		return ikeRepository.findAssociatedSemanticIds(id);
	}

	public String retrieveFullyQualifiedName(UUID id) {
		return calculatorService.calculateFQN(id);
	}

	public String retrieveSynonym(UUID id) {
		return calculatorService.calculateSYN(id);
	}

	public String retrieveDefinition(UUID id) {
		return calculatorService.calculateDEF(id);
	}

	public List<List<UUID>> retrieveChildren(UUID id) {
		return calculatorService.calculateChildren(id);
	}

	public List<List<UUID>> retrieveParents(UUID id) {
		return calculatorService.calculateParents(id);
	}

	public List<List<UUID>> retrieveDescendants(UUID id) {
		return calculatorService.calculateDescendants(id);
	}

	public List<List<UUID>> retrieveAncestors(UUID id) {
		return calculatorService.calculateAncestors(id);
	}

	public List<List<UUID>> retrieveKinds(UUID id) {
		return calculatorService.calculateKinds(id);
	}

	public List<IdentifierSemanticDTO> retrieveIdentifiers(UUID id) {
		return ikeRepository.findIdentifiers(id).entrySet().stream()
				.map(entry -> new IdentifierSemanticDTO(entry.getValue(), entry.getKey()))
				.toList();
	}
}
