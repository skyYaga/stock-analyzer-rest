package eu.yaga.stockanalyzer.util;

/**
 * A utility class that contains the quandl code and the currency of the resource
 */
public class QuandlCode {
    private String code;
    private String currency;

    public QuandlCode(String code, String currency) {
        this.code = code;
        this.currency = currency;
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
}
