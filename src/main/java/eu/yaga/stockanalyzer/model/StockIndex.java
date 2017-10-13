package eu.yaga.stockanalyzer.model;

/**
 * Enum containing indices
 */
public enum StockIndex {
    DAX("^GDAXI", "DAX", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=20735&interval=D1&assetName=DAX&exchange=Xetra&dateStart="),
    MDAX("^MDAXI", "MDAX", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=323547&interval=D1&assetName=MDAX&exchange=Xetra&dateStart="),
    SDAX("^SDAXI", "SDAX", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=324724&interval=D1&assetName=SDAX&exchange=Xetra&dateStart="),
    TECDAX("^TECDAX", "TecDAX", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=6623216&interval=D1&assetName=TecDAX&exchange=Xetra&dateStart="),
    SP500("^GSPC", "S&P 500", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=4359526&interval=D1&assetName=S%2526amp%253BP%2520500&exchange=au%C3%9Ferb%C3%B6rslich&dateStart="),
    NASDAQ("^IXIC", "NASDAQ", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=325104&interval=D1&assetName=NASDAQ&exchange=Nasdaq+Global+Indices&dateStart="),
    EUROSTOXX50("^STOXX50E", "EURO STOXX 50", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=193736&interval=D1&assetName=EURO%2520STOXX%252050&exchange=au%C3%9Ferb%C3%B6rslich&dateStart="),
    FTSE100("^FTSE", "FTSE 100", "http://www.onvista.de/onvista/boxes/popup/historicalquote.json?notationId=1918069&interval=D1&assetName=FTSE&exchange=au%C3%9Ferb%C3%B6rslich&dateStart=");

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
