package eu.yaga.stockanalyzer.model.historicaldata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Historical stock exchange rate
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class HistoricalDataQuote {
    private String symbol;
    private String date;
    private double close;

    public HistoricalDataQuote() {}

    public HistoricalDataQuote(String symbol, String date, double close) {
        this.symbol = symbol;
        this.date = date;
        this.close = close;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    @Override
    public String toString() {
        return "ExchangeRate{" +
                "symbol='" + symbol + '\'' +
                ", date=" + date +
                ", close=" + close +
                '}';
    }
}
