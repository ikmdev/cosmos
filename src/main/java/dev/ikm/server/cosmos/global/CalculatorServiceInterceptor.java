package dev.ikm.server.cosmos.global;

import dev.ikm.server.cosmos.calculator.CalculatorService;
import dev.ikm.server.cosmos.database.CosmosDatabaseConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

import java.util.UUID;

@Component
public class CalculatorServiceInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(CalculatorServiceInterceptor.class);
    public static final String ACTIVE_OBSERVATORY_ID_ATTR = "activeObservatoryId";

    private final CalculatorService calculatorService;

    @Autowired
    public CalculatorServiceInterceptor(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Cookie cookie = WebUtils.getCookie(request, "cosmos-observatory-id");
        UUID activeObservatoryId;

        if (cookie == null || cookie.getValue().isBlank()) {
            activeObservatoryId = CosmosDatabaseConfig.DEFAULT_OBSERVATORY_ID;
        } else {
            try {
                activeObservatoryId = UUID.fromString(cookie.getValue());
            } catch (IllegalArgumentException e) {
                activeObservatoryId = CosmosDatabaseConfig.DEFAULT_OBSERVATORY_ID;
                LOG.warn("Invalid UUID in 'cosmos-observatory-id' cookie: '{}'. Falling back to default.", cookie.getValue());
            }
        }

        // This is guaranteed to run before controller data binding, setting up the request-scoped bean.
        calculatorService.setObservatory(activeObservatoryId);
        // Make the active observatory ID available to other components, like the GlobalControllerAdvice.
        request.setAttribute(ACTIVE_OBSERVATORY_ID_ATTR, activeObservatoryId);
        return true; // Continue processing the request
    }
}