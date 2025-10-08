package dev.ikm.server.cosmos.concept;

import dev.ikm.server.cosmos.database.Context;
import dev.ikm.server.cosmos.database.IkeRepository;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.ConceptEntityVersion;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.terms.TinkarTermV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConceptService {

	private final IkeRepository ikeRepository;
	private final Context context;

	@Autowired
	public ConceptService(IkeRepository ikeRepository, Context context) {
		this.ikeRepository = ikeRepository;
		this.context = context;
	}

	public ConceptChronologyDTO retrieveConceptWithAllVersions(UUID uuid) {
		Optional<ConceptEntity<? extends ConceptEntityVersion>> optionalConceptEntity = ikeRepository.findConceptById(PublicIds.of(uuid));
		return new ConceptChronologyDTO(
				PublicIds.of(uuid).asUuidList().toList(),
				null,
				optionalConceptEntity.map(conceptEntity -> conceptEntity.versions().stream()
								.map(conceptEntityVersion ->
										new ConceptVersionDTO(conceptEntityVersion.stamp().publicId().asUuidList().toList()))
								.toList())
						.orElse(Collections.emptyList())
		);
	}

	public ConceptChronologyDTO retrieveConceptWithLatestVersion(UUID uuid) {
		Latest<ConceptEntityVersion> latestConceptEntityVersion = ikeRepository.findLatestConceptById(PublicIds.of(uuid), context.getStampCalculator());
		if (latestConceptEntityVersion.isPresent()) {
			ConceptEntityVersion conceptEntityVersion = latestConceptEntityVersion.get();
			return new ConceptChronologyDTO(
					PublicIds.of(uuid).asUuidList().toList(),
					new ConceptVersionDTO(
							conceptEntityVersion.stamp().publicId().asUuidList().toList()
					),
					null);
		} else {
			return new ConceptChronologyDTO(
					PublicIds.of(uuid).asUuidList().toList(),
					null,
					null);
		}
	}

	public List<List<UUID>> retrieveSemantics(UUID uuid) {
		List<List<UUID>> semanticIds = new ArrayList<>();
		PublicId conceptPublicId = PublicIds.of(uuid);
		PrimitiveData.get().forEachSemanticNidForComponent(
				Entity.nid(conceptPublicId),
				semanticNid -> {
					semanticIds.add(PrimitiveData.publicId(semanticNid).asUuidList().toList());
				});
		return semanticIds;
	}

	public String calculateFQN(UUID uuid) {
		PublicId conceptPublicId = PublicIds.of(uuid);
		return context.getLanguageCalculator()
				.getFullyQualifiedNameText(Entity.nid(conceptPublicId))
				.orElse("");
	}

	public String calculateSYN(UUID uuid) {
		PublicId conceptPublicId = PublicIds.of(uuid);
		return context.getLanguageCalculator()
				.getRegularDescriptionText(Entity.nid(conceptPublicId))
				.orElse("");
	}

	public String calculateDEF(UUID uuid) {
		PublicId conceptPublicId = PublicIds.of(uuid);
		return context.getLanguageCalculator()
				.getDefinitionDescriptionText(Entity.nid(conceptPublicId))
				.orElse("");
	}

	public List<List<UUID>> calculateChildren(UUID uuid) {
		PublicId conceptPublicId = PublicIds.of(uuid);
		return context.getNavigationCalculator()
				.childrenOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateParents(UUID uuid) {
		PublicId conceptPublicId = PublicIds.of(uuid);
		return context.getNavigationCalculator()
				.parentsOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateDescendants(UUID uuid) {
		PublicId conceptPublicId = PublicIds.of(uuid);
		return context.getNavigationCalculator()
				.descendentsOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateAncestors(UUID uuid) {
		PublicId conceptPublicId = PublicIds.of(uuid);
		return context.getNavigationCalculator()
				.ancestorsOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<List<UUID>> calculateKinds(UUID uuid) {
		PublicId conceptPublicId = PublicIds.of(uuid);
		return context.getNavigationCalculator()
				.kindOf(Entity.nid(conceptPublicId))
				.mapToList(PrimitiveData::publicId)
				.stream()
				.map(publicId -> publicId.asUuidList().stream().toList())
				.toList();
	}

	public List<IdentifierSemanticDTO> calculateIdentifiers(UUID uuid) {
		List<IdentifierSemanticDTO> identifiers = new ArrayList<>();
		PublicId conceptPublicId = PublicIds.of(uuid);
		PrimitiveData.get().forEachSemanticNidForComponentOfPattern(
				Entity.nid(conceptPublicId),
				TinkarTermV2.IDENTIFIER_PATTERN.nid(),
				identifierSemanticNid -> {
					Latest<Field<PublicId>> latestSource = context.getStampCalculator().getFieldForSemanticWithMeaning(identifierSemanticNid, TinkarTermV2.IDENTIFIER_SOURCE);
					Latest<Field<String>> latestValue = context.getStampCalculator().getFieldForSemanticWithMeaning(identifierSemanticNid, TinkarTermV2.IDENTIFIER_VALUE);
					if (latestSource.isPresent() && latestValue.isPresent()) {
						identifiers.add(new IdentifierSemanticDTO(latestValue.get().value(), latestSource.get().value().asUuidList().toList()));
					}
				}
		);
		return identifiers;
	}
}
