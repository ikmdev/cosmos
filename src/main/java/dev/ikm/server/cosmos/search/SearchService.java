package dev.ikm.server.cosmos.search;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.Id;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.server.cosmos.ike.Type;
import dev.ikm.tinkar.common.util.text.NaturalOrder;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.LatestVersionSearchResult;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

	public Page<Facade> search(String query, Pageable pageable, SortType sortType, Function<Facade, Boolean> filter) {
		List<Facade> allSearchResults = switch (sortType) {
			case NATURAL_ORDER -> search(query, naturalOrderComparator());
			case SEMANTIC_SCORE -> search(query, semanticScoreComparator());
		};
		List<Facade> filteredFacades = allSearchResults.stream()
				.filter(filter::apply)
				.toList();

		List<Facade> paginatedFilteredFacades = filteredFacades.stream()
				.skip(pageable.getOffset())
				.limit(pageable.getPageSize())
				.toList();
		return new PageImpl<>(paginatedFilteredFacades, pageable, filteredFacades.size());
	}

	public Page<Facade> search(String query, Pageable pageable, SortType sortType) {
		return search(query, pageable, sortType, facade -> true);
	}

	public Page<Facade> searchPatterns(String query, Pageable pageable, SortType sortType) {
		return search(query, pageable, sortType, facade -> facade.type() == Type.PATTERN);
	}

	public Page<Facade> searchConcepts(String query, Pageable pageable, SortType sortType) {
		return search(query, pageable, sortType, facade -> facade.type() == Type.CONCEPT);
	}

	private List<Facade> search(String query, Comparator<LatestVersionSearchResult> comparator) {
		try {
			return calculatorService.getStampCalculator().search(query, 10_000)
					.stream()
					.sorted(comparator)
					.map(this::transformToReferenceFacade)
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

	private Facade transformToReferenceFacade(LatestVersionSearchResult latestVersionSearchResult) {
		Latest<SemanticEntityVersion> semanticEntityVersionLatest = latestVersionSearchResult.latestVersion();
		if (semanticEntityVersionLatest.isPresent()) {
			int refComponentNid = semanticEntityVersionLatest.get().referencedComponentNid();
			Optional<Entity<EntityVersion>> optionalEntity = Entity.get(refComponentNid);
			if (optionalEntity.isPresent()) {
				Entity<EntityVersion> entity = optionalEntity.get();
				switch (entity) {
					case ConceptEntity conceptEntity -> {
						return new Facade(
								new Id(conceptEntity.nid(), conceptEntity.publicId().asUuidList().castToList()),
								Type.CONCEPT,
								calculatorService.calculateText(conceptEntity.publicId()));
					}
					case SemanticEntity semanticEntity -> {
						return new Facade(
								new Id(semanticEntity.nid(), semanticEntity.publicId().asUuidList().castToList()),
								Type.SEMANTIC,
								calculatorService.calculateText(semanticEntity.publicId()));
					}
					case PatternEntity patternEntity -> {
						return new Facade(
								new Id(patternEntity.nid(), patternEntity.publicId().asUuidList().castToList()),
								Type.PATTERN,
								calculatorService.calculateText(patternEntity.publicId()));
					}
					case StampEntity stamp -> {
						return new Facade(
								new Id(stamp.nid(), stamp.publicId().asUuidList().castToList()),
								Type.STAMP,
								calculatorService.calculateText(stamp.publicId()));
					}
					default -> throw new IllegalStateException("Unknown entity type");
				}
			}
		}
		throw new IllegalStateException("Semantic entity version not found for latest version of search result");
	}

}
