package eu.yaga.stockanalyzer.model;

/**
 * Type for categorized URLs
 */
public class FundamentalDataUrl {

    private FundamentalDataUrlType type;
    private String url;

    public FundamentalDataUrl() {
        this.url = "";
        this.type = FundamentalDataUrlType.OTHER;
    }

    // Legacy support
    public FundamentalDataUrl(String url) {
        this.url = url;
        this.type = FundamentalDataUrlType.OTHER;
    }

    public FundamentalDataUrlType getType() {
        return type;
    }

    public void setType(FundamentalDataUrlType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
