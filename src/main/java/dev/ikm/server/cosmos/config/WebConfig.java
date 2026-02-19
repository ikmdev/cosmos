package dev.ikm.server.cosmos.config;

import dev.ikm.server.cosmos.global.CalculatorServiceInterceptor;
import dev.ikm.server.cosmos.observatory.StringToFacadeFormatterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CalculatorServiceInterceptor calculatorServiceInterceptor;
    private final StringToFacadeFormatterFactory stringToFacadeFormatterFactory;

    @Autowired
    public WebConfig(StringToFacadeFormatterFactory stringToFacadeFormatterFactory, CalculatorServiceInterceptor calculatorServiceInterceptor) {
        this.stringToFacadeFormatterFactory = stringToFacadeFormatterFactory;
        this.calculatorServiceInterceptor = calculatorServiceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(calculatorServiceInterceptor);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldAnnotation(stringToFacadeFormatterFactory);
    }
}