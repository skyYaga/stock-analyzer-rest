package eu.yaga.stockanalyzer.model;

/**
 * Created by andreas on 13.11.16.
 */
public enum StockIndex {
    DAX("^GDAXI", "DAX", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=20735&interval=D1&assetName=DAX&exchange=Xetra&dateStart="),
    MDAX("^MDAXI", "MDAX", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=323547&interval=D1&assetName=MDAX&exchange=Xetra&dateStart="),
    TECDAX("^TECDAX", "TecDAX", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=6623216&interval=D1&assetName=TecDAX&exchange=Xetra&dateStart="),
    SP500("^GSPC", "S&P 500", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=4359526&interval=D1&assetName=S%2526amp%253BP%2520500&exchange=au%C3%9Ferb%C3%B6rslich&dateStart=");

    private final String symbol;
    private final String description;
    private final String onvistaApiUrl;


    StockIndex(String symbol, String description, String onvistaApiUrl) {
        this.symbol = symbol;
        this.description = description;
        this.onvistaApiUrl = onvistaApiUrl;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    public String getOnvistaApiUrl() {
        return onvistaApiUrl;
    }
}
