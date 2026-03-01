package dev.ikm.server.cosmos.observatory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ObservatoryDataInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ObservatoryDataInitializer.class);
    private final ObservatoryService observatoryService;

    @Autowired
    public ObservatoryDataInitializer(ObservatoryService observatoryService) {
        this.observatoryService = observatoryService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        LOG.info("Application is ready. Verifying default observatory...");
        try {
            observatoryService.bootstrapDefaultObservatory();
        } catch (Exception e) {
            LOG.error("FATAL: Failed to bootstrap the default observatory. This may cause application instability.", e);
        }
    }
}