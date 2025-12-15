package dev.ikm.server.cosmos.api.coordinate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToCoordinateDTOConverter implements Converter<String, CoordinateDTO> {

    private final CoordinateService coordinateService;

    @Autowired
    public StringToCoordinateDTOConverter(CoordinateService coordinateService) {
        this.coordinateService = coordinateService;
    }

    @Override
    public CoordinateDTO convert(String source) {
        return coordinateService.stampCoordinates().stream()
                .filter(dto -> dto.name().equals(source))
                .findFirst()
                .orElseGet(() -> coordinateService.languageCoordinates().stream()
                        .filter(dto -> dto.name().equals(source))
                        .findFirst()
                        .orElseGet(() -> coordinateService.navigationCoordinates().stream()
                                .filter(dto -> dto.name().equals(source))
                                .findFirst()
                                .orElse(null)));
    }
}
