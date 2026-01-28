package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.EntityVersion;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

	private final CalculatorService calculatorService;
	private final IkeRepository ikeRepository;

	public enum SortType {
		NATURAL_ORDER,
		SEMANTIC_SCORE
	}

	@Autowired
	public SearchService(CalculatorService calculatorService, IkeRepository ikeRepository) {
		this.calculatorService = calculatorService;
		this.ikeRepository = ikeRepository;
	}

	public List<SearchResult> tinkarDataSearch(String query, int maxResults, SortType sortType) {
		ImmutableList<LatestVersionSearchResult> unsortedResults = search(query, maxResults);
		return switch (sortType) {
			case NATURAL_ORDER -> transformToSearchResults(sortResultByNaturalOrder(unsortedResults));
			case SEMANTIC_SCORE -> transformToSearchResults(sortResultsBySemanticScore(unsortedResults));
		};
	}

	private List<SearchResult> transformToSearchResults(ImmutableList<LatestVersionSearchResult> results) {
		return results.stream()
				.map(LatestVersionSearchResult::latestVersion)
				.filter(Latest::isPresent)
				.map(Latest::get)
				.map(EntityVersion::publicId)
				.map(publicId -> new SearchResult(ikeRepository.getIds(publicId), calculatorService.calculateText(publicId)))
				.toList();
	}

	private ImmutableList<LatestVersionSearchResult> search(String query, int maxResults) {
		try {
			return calculatorService.getStampCalculator().search(query, maxResults);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ImmutableList<LatestVersionSearchResult> sortResultsBySemanticScore(ImmutableList<LatestVersionSearchResult> results) {
		return results
				.toSortedList((o1, o2) -> Float.compare(o2.score(), o1.score()))
				.toImmutable();
	}

	private ImmutableList<LatestVersionSearchResult> sortResultByNaturalOrder(ImmutableList<LatestVersionSearchResult> results) {
		return results
				.toSortedList((o1, o2) -> {
					String string1 = (String) o1.latestVersion().get().fieldValues().get(o1.fieldIndex());
					String string2 = (String) o2.latestVersion().get().fieldValues().get(o2.fieldIndex());
					return NaturalOrder.compareStrings(string1, string2);
				})
				.toImmutable();
	}
}
