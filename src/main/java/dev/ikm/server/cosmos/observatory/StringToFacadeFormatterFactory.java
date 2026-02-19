package dev.ikm.server.cosmos.observatory;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.calculator.Language;
import dev.ikm.server.cosmos.calculator.Navigation;
import dev.ikm.server.cosmos.calculator.Stamp;
import dev.ikm.server.cosmos.ike.Facade;
import dev.ikm.server.cosmos.ike.Id;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory for creating formatters that convert between a String (representing a NID)
 * and a {@link Facade} object, based on the {@link StringToFacade} annotation.
 */
@Component
public class StringToFacadeFormatterFactory implements AnnotationFormatterFactory<StringToFacade> {

    private final CalculatorService calculatorService;

    @Autowired
    public StringToFacadeFormatterFactory(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Override
    public Set<Class<?>> getFieldTypes() {
        // This factory is applicable to fields of type Facade.
        return Set.of(Facade.class);
    }

    @Override
    public Printer<Facade> getPrinter(StringToFacade annotation, Class<?> fieldType) {
        // Converts a Facade object back to a String (its NID) for rendering in views.
        return (facade, locale) -> String.valueOf(facade.id().nid());
    }

    @Override
    public Parser<Facade> getParser(StringToFacade annotation, Class<?> fieldType) {
        // Parses a String from the request (the NID) into a full Facade object.
        return (text, locale) -> {

            if (text == null || text.trim().isEmpty()) {
                // This can happen with empty form submissions, so a debug/trace level is more appropriate.
                return null;
            }

            final int nid;
            try {
                nid = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                // The submitted value was not a valid integer, so we cannot convert it.
                return null;
            }

            // 1. Check for well-known facades (from enums) using the pre-loaded map.
            for (Stamp stamp : Stamp.values()) {
                if (stamp.getNid() == nid) {
                    return stamp.getConcept();
                }
            }
            for (Language language : Language.values()) {
                if (language.getNid() == nid) {
                    return language.getConcept();
                }
            }
            for (Navigation navigation : Navigation.values()) {
                if (navigation.getNid() == nid) {
                    return navigation.getConcept();
                }
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
            return null;
        };
    }
}