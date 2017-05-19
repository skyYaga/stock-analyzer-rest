package eu.yaga.stockanalyzer.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Automatically loaded properties for sending emails
 */
@ConfigurationProperties(prefix = "email")
@Validated
@Component
public class EmailProperties {

    private String mjApikeyPublic;
    private String mjApikeyPrivate;
    private String fromEmail;
    private String fromName;
    private List<String> listRecipient;

    public String getMjApikeyPublic() {
        return mjApikeyPublic;
    }

    public void setMjApikeyPublic(String mjApikeyPublic) {
        this.mjApikeyPublic = mjApikeyPublic;
    }

    public String getMjApikeyPrivate() {
        return mjApikeyPrivate;
    }

    public void setMjApikeyPrivate(String mjApikeyPrivate) {
        this.mjApikeyPrivate = mjApikeyPrivate;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public List<String> getListRecipient() {
        return listRecipient;
    }

    public void setListRecipient(List<String> listRecipient) {
        this.listRecipient = listRecipient;
    }
}
