package eu.yaga.stockanalyzer.service.mock;

import eu.yaga.stockanalyzer.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation of the Email service
 */
public class EmailServiceMock implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceMock.class);

    @Override
    public void send(String subject, String text) {
        log.info("Mock: Sending mail...");
    }
}
