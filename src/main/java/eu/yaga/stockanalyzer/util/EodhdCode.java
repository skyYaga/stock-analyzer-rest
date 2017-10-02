package eu.yaga.stockanalyzer.util;

/**
 * A utility class that contains the quandl code and the currency of the resource
 */
public class EodhdCode {
    private String symbol;
    private String code;
    private String currency;
    private String closeColumnName;

    public EodhdCode(String symbol, String code, String currency, String closeColumnName) {
        this.symbol = symbol;
        this.code = code;
        this.currency = currency;
        this.closeColumnName = closeColumnName;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
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
