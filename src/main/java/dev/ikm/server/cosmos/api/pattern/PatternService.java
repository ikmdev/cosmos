package dev.ikm.server.cosmos.api.pattern;

import dev.ikm.server.cosmos.api.calculator.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
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
	private final CalculatorService calculatorService;

	@Autowired
	public PatternService(IkeRepository ikeRepository, CalculatorService calculatorService) {
		this.ikeRepository = ikeRepository;
		this.calculatorService = calculatorService;
	}

	public PatternChronologyDTO retrievePatternWithAllVersions(UUID id) {
		Optional<PatternEntity<? extends PatternEntityVersion>> optionalPatternEntity = ikeRepository.findPatternById(id);
		return new PatternChronologyDTO(
				ikeRepository.getIds(id),
				null,
				optionalPatternEntity.map(patternEntity ->
								patternEntity.versions().stream()
										.map(patternEntityVersion ->
												new PatternVersionDTO(
														ikeRepository.getIds(patternEntityVersion.stamp()),
														ikeRepository.getIds(patternEntityVersion.semanticMeaning()),
														ikeRepository.getIds(patternEntityVersion.semanticPurpose()),
														patternEntityVersion.fieldDefinitions().stream()
																.map(fieldDefinitionForEntity ->
																		new FieldDefinitionDTO(
																				ikeRepository.getIds(fieldDefinitionForEntity.dataType()),
																				ikeRepository.getIds(fieldDefinitionForEntity.meaning()),
																				ikeRepository.getIds(fieldDefinitionForEntity.purpose()),
																				fieldDefinitionForEntity.indexInPattern()
																		)
																)
																.toList()
												))
										.toList())
						.orElse(Collections.emptyList())
		);
	}

	public PatternChronologyDTO retrievePatternWithLatestVersion(UUID id) {
		Latest<PatternEntityVersion> latestPatternEntityVersion = ikeRepository.findLatestPatternById(id);
		if (latestPatternEntityVersion.isPresent()) {
			PatternEntityVersion patternEntityVersion = latestPatternEntityVersion.get();
			return new PatternChronologyDTO(
					ikeRepository.getIds(id),
					new PatternVersionDTO(
							ikeRepository.getIds(patternEntityVersion.stamp()),
							ikeRepository.getIds(patternEntityVersion.semanticMeaning()),
							ikeRepository.getIds(patternEntityVersion.semanticPurpose()),
							patternEntityVersion.fieldDefinitions().stream()
									.map(fieldDefinitionForEntity ->
											new FieldDefinitionDTO(
													ikeRepository.getIds(fieldDefinitionForEntity.dataType()),
													ikeRepository.getIds(fieldDefinitionForEntity.meaning()),
													ikeRepository.getIds(fieldDefinitionForEntity.purpose()),
													fieldDefinitionForEntity.indexInPattern()
											)
									)
									.toList()
					),
					null
			);
		} else {
			return new PatternChronologyDTO(
					ikeRepository.getIds(id),
					null,
					null
			);
		}
	}

	public List<List<UUID>> retrieveSemantics(UUID id) {
		return ikeRepository.findAssociatedSemanticIds(id);
	}

	public String calculateFQN(UUID id) {
		return calculatorService.calculateFQN(id);
	}

	public String calculateSYN(UUID id) {
		return calculatorService.calculateSYN(id);
	}

	public String calculateDEF(UUID id) {
		return calculatorService.calculateDEF(id);
	}
}
