package eu.yaga.stockanalyzer.util;

/**
 * A utility class that contains the quandl code and the currency of the resource
 */
public class QuandlCode {
    private String code;
    private String currency;
    private String closeColumnName;

    public QuandlCode(String code, String currency, String closeColumnName) {
        this.code = code;
        this.currency = currency;
        this.closeColumnName = closeColumnName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCloseColumnName() {
        return closeColumnName;
    }

    public void setCloseColumnName(String closeColumnName) {
        this.closeColumnName = closeColumnName;
    }
}
