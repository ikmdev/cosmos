package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.EntityVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SearchService {

	private final CalculatorService calculatorService;
	private final IkeRepository ikeRepository;

	@Autowired
	public SearchService(CalculatorService calculatorService, IkeRepository ikeRepository) {
		this.calculatorService = calculatorService;
		this.ikeRepository = ikeRepository;
	}

	public List<SearchResultDTO> search(String query) {
		List<LatestVersionSearchResult> results = new ArrayList<>();
		try {
			results.addAll(calculatorService.getStampCalculator().search(query, 1000).castToList());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return results.stream()
				.map(LatestVersionSearchResult::latestVersion)
				.filter(Latest::isPresent)
				.map(Latest::get)
				.map(EntityVersion::publicId)
				.map(publicId ->
						new SearchResultDTO(ikeRepository.getIds(publicId),
								calculatorService.calculateText(publicId)))
				.toList();
	}
}
