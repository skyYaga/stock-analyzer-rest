package eu.yaga.stockanalyzer.model.eodhd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON result object for eodhistoricaldata.com queries
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EodhdQuote {

    private String date;
    private double close;

    public EodhdQuote() {}

    public EodhdQuote(String date, double close) {
        this.date = date;
        this.close = close;
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
        return "EodhdQuote{" +
                "date='" + date + '\'' +
                ", close=" + close +
                '}';
    }
}
