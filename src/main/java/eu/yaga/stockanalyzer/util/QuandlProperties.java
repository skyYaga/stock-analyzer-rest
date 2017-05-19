package eu.yaga.stockanalyzer.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Automatically loaded properties for sending emails
 */
@ConfigurationProperties(prefix = "quandl")
@Validated
@Component
public class QuandlProperties {

    public static class Auth {

        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    private Auth auth;

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }
}
