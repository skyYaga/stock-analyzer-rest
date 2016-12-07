package eu.yaga.stockanalyzer.config;

import eu.yaga.stockanalyzer.service.EmailService;
import eu.yaga.stockanalyzer.service.mock.EmailServiceMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring Application Config
 */
@Configuration
@Profile("dev")
public class ApplicationConfigDev {

    @Bean
    public EmailService getEmailService() {
        return new EmailServiceMock();
    }

}
