package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

	private final CalculatorService calculatorService;

	@Autowired
	public SearchService(CalculatorService calculatorService) {
		this.calculatorService = calculatorService;
	}

	public List<SearchResultDTO> search(String query) {
		List<LatestVersionSearchResult> results = new ArrayList<>();
		try {
			results.addAll(calculatorService.getStampCalculator().search(query, 100).castToList());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return null;
	}
}
