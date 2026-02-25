package dev.ikm.server.cosmos.discovery;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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

	//TODO-need to overhaul this to return a Facade aware record. SearchResult is using List<UUID> to represent publicIds
	public <T> List<T> tinkarDataSearch(String query, int maxResults, SortType sortType, Function<SearchResult, T> transformer) {
		return tinkarDataSearch(query, PageRequest.of(0, maxResults), sortType, transformer).getContent();
	}

	public <T> Page<T> tinkarDataSearch(String query, Pageable pageable, SortType sortType, Function<SearchResult, T> transformer) {
		List<SearchResult> allSearchResults = switch (sortType) {
			case NATURAL_ORDER -> search(query, naturalOrderComparator());
			case SEMANTIC_SCORE -> search(query, semanticScoreComparator());
		};
		List<T> searchResults = allSearchResults.stream()
				.skip(pageable.getOffset())
				.limit(pageable.getPageSize())
				.map(transformer)
				.toList();
		return new PageImpl<>(searchResults, pageable, allSearchResults.size());
	}

	private List<SearchResult> search(String query, Comparator<LatestVersionSearchResult> comparator) {
		try {
			return calculatorService.getStampCalculator().search(query, 10_000)
					.stream()
					.sorted(comparator)
					.map(this::transformToSearchResults)
					.toList();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Comparator<LatestVersionSearchResult> semanticScoreComparator() {
		return (o1, o2) -> Float.compare(o2.score(), o1.score());
	}

	private Comparator<LatestVersionSearchResult> naturalOrderComparator() {
		return (o1, o2) -> {
			String string1 = (String) o1.latestVersion().get().fieldValues().get(o1.fieldIndex());
			String string2 = (String) o2.latestVersion().get().fieldValues().get(o2.fieldIndex());
			return NaturalOrder.compareStrings(string1, string2);
		};
	}

	private SearchResult transformToSearchResults(LatestVersionSearchResult latestVersionSearchResult) {
		Latest<SemanticEntityVersion> semanticEntityVersionLatest = latestVersionSearchResult.latestVersion();
		if (semanticEntityVersionLatest.isPresent()) {
			return new SearchResult(
					ikeRepository.getIds(semanticEntityVersionLatest.get().publicId()),
					calculatorService.calculateText(semanticEntityVersionLatest.get().publicId()));
		} else {
			throw new IllegalStateException("Semantic entity version not found for latest version of search result");
		}
	}

}
