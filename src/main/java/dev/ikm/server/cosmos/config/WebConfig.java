package dev.ikm.server.cosmos.config;

import dev.ikm.server.cosmos.global.ObservatoryContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ObservatoryContextInterceptor observatoryContextInterceptor;

    @Autowired
    public WebConfig(ObservatoryContextInterceptor observatoryContextInterceptor) {
        this.observatoryContextInterceptor = observatoryContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(observatoryContextInterceptor);
    }
}