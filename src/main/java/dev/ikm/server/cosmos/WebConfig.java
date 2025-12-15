package dev.ikm.server.cosmos;

import dev.ikm.server.cosmos.api.coordinate.StringToCoordinateDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private StringToCoordinateDTOConverter stringToCoordinateDTOConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToCoordinateDTOConverter);
    }
}
