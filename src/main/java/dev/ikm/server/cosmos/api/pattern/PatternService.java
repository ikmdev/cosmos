package dev.ikm.server.cosmos.api.pattern;

import dev.ikm.server.cosmos.database.Context;
import dev.ikm.server.cosmos.database.IkeRepository;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PatternService {

	private final IkeRepository ikeRepository;
	private final Context context;

	@Autowired
	public PatternService(IkeRepository ikeRepository, Context context) {
		this.ikeRepository = ikeRepository;
		this.context = context;
	}

	public PatternChronologyDTO retrievePatternWithAllVersions(UUID uuid) {
		Optional<PatternEntity<? extends PatternEntityVersion>> optionalPatternEntity = ikeRepository.findPatternById(PublicIds.of(uuid));
		return new PatternChronologyDTO(
				PublicIds.of(uuid).asUuidList().toList(),
				null,
				optionalPatternEntity.map(patternEntity ->
								patternEntity.versions().stream()
										.map(patternEntityVersion ->
												new PatternVersionDTO(
														patternEntityVersion.stamp().publicId().asUuidList().toList(),
														patternEntityVersion.semanticMeaning().publicId().asUuidList().toList(),
														patternEntityVersion.semanticPurpose().publicId().asUuidList().toList(),
														patternEntityVersion.fieldDefinitions().stream()
																.map(fieldDefinitionForEntity ->
																		new FieldDefinitionDTO(
																				fieldDefinitionForEntity.dataType().publicId().asUuidList().toList(),
																				fieldDefinitionForEntity.meaning().publicId().asUuidList().toList(),
																				fieldDefinitionForEntity.purpose().publicId().asUuidList().toList(),
																				fieldDefinitionForEntity.indexInPattern()
																		)
																)
																.toList()
												))
										.toList())
						.orElse(Collections.emptyList())
		);
	}

	public PatternChronologyDTO retrievePatternWithLatestVersion(UUID uuid) {
		Latest<PatternEntityVersion> latestPatternEntityVersion = ikeRepository.findLatestPatternById(PublicIds.of(uuid), context.getStampCalculator());
		if (latestPatternEntityVersion.isPresent()) {
			PatternEntityVersion patternEntityVersion = latestPatternEntityVersion.get();
			return new PatternChronologyDTO(
					patternEntityVersion.publicId().asUuidList().toList(),
					new PatternVersionDTO(
							patternEntityVersion.stamp().publicId().asUuidList().toList(),
							patternEntityVersion.semanticMeaning().publicId().asUuidList().toList(),
							patternEntityVersion.semanticPurpose().publicId().asUuidList().toList(),
							patternEntityVersion.fieldDefinitions().stream()
									.map(fieldDefinitionForEntity ->
											new FieldDefinitionDTO(
													fieldDefinitionForEntity.dataType().publicId().asUuidList().toList(),
													fieldDefinitionForEntity.meaning().publicId().asUuidList().toList(),
													fieldDefinitionForEntity.purpose().publicId().asUuidList().toList(),
													fieldDefinitionForEntity.indexInPattern()
											)
									)
									.toList()
					),
					null
			);
		} else {
			return new PatternChronologyDTO(
					PublicIds.of(uuid).asUuidList().toList(),
					null,
					null
			);
		}
	}

	public List<List<UUID>> retrieveSemantics(UUID uuid) {
		List<List<UUID>> patternIds = new ArrayList<>();
		PublicId conceptPublicId = PublicIds.of(uuid);
		PrimitiveData.get().forEachSemanticNidForComponent(
				Entity.nid(conceptPublicId),
				semanticNid -> {
					patternIds.add(PrimitiveData.publicId(semanticNid).asUuidList().toList());
				});
		return patternIds;
	}

	public String calculateFQN(UUID uuid) {
		PublicId patternPublicId = PublicIds.of(uuid);
		return context.getLanguageCalculator()
				.getFullyQualifiedNameText(Entity.nid(patternPublicId))
				.orElse("");
	}

	public String calculateSYN(UUID uuid) {
		PublicId patternPublicId = PublicIds.of(uuid);
		return context.getLanguageCalculator()
				.getRegularDescriptionText(Entity.nid(patternPublicId))
				.orElse("");
	}

	public String calculateDEF(UUID uuid) {
		PublicId patternPublicId = PublicIds.of(uuid);
		return context.getLanguageCalculator()
				.getDefinitionDescriptionText(Entity.nid(patternPublicId))
				.orElse("");
	}

}
