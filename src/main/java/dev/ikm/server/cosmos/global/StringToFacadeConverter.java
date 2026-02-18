package dev.ikm.server.cosmos.global;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.calculator.Language;
import dev.ikm.server.cosmos.calculator.Navigation;
import dev.ikm.server.cosmos.calculator.Stamp;
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.Id;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@org.springframework.stereotype.Component
public class StringToFacadeConverter implements Converter<String, Facade> {

	private static final Logger LOG = LoggerFactory.getLogger(StringToFacadeConverter.class);
	private static final Map<Integer, Facade> WELL_KNOWN_FACADES = new ConcurrentHashMap<>();

	// Static initializer to pre-load the well-known facades from enums for O(1) lookup.
	static {
		for (Stamp stamp : Stamp.values()) {
			WELL_KNOWN_FACADES.put(stamp.getNid(), stamp.getConcept());
		}
		for (Language language : Language.values()) {
			WELL_KNOWN_FACADES.put(language.getNid(), language.getConcept());
		}
		for (Navigation navigation : Navigation.values()) {
			WELL_KNOWN_FACADES.put(navigation.getNid(), navigation.getConcept());
		}
	}

	private final CalculatorService calculatorService;

	@Autowired
	public StringToFacadeConverter(CalculatorService calculatorService) {
		this.calculatorService = calculatorService;
	}

	@Override
	public Facade convert(String source) {
		LOG.debug("Attempting to convert source: '{}'", source);
		if (source == null || source.trim().isEmpty()) {
			// This can happen with empty form submissions, so a debug/trace level is more appropriate.
			LOG.trace("Cannot convert empty or null string to Facade");
			return null;
		}

		final int nid;
		try {
			nid = Integer.parseInt(source);
		} catch (NumberFormatException e) {
			// The submitted value was not a valid integer, so we cannot convert it.
			LOG.warn("Cannot convert '{}' to Facade because it's not a valid integer", source);
			return null;
		}

		// 1. Check for well-known facades (from enums) using the pre-loaded map.
		Facade wellKnownFacade = WELL_KNOWN_FACADES.get(nid);
		if (wellKnownFacade != null) {
			return wellKnownFacade;
		}

		// 2. If not in enums, look for Facade nid in the entity database
		Optional<Entity<EntityVersion>> entityOptional = Entity.get(nid);
		if (entityOptional.isPresent()) {
			// Any entity found by its nid can be converted to a Facade.
			Entity<? extends EntityVersion> entity = entityOptional.get();
			return new Facade(
					new Id(entity.nid(), entity.publicId().asUuidList().castToList()),
					calculatorService.calculateText(entity.publicId()));
		}

		LOG.warn("Could not find any entity or enum for nid {} to convert to Facade.", nid);
		return null;
	}
}
