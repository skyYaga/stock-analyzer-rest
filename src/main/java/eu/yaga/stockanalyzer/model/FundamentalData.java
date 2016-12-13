package eu.yaga.stockanalyzer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A POJO for fundamental data
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundamentalData {

    @Id
    private String id;
    private Date date;

    private String symbol;
    private StockType stockType;
    private ArrayList<String> businessYears = new ArrayList<>();
    private double roe;
    private double ebit;
    private double equityRatio;
    private double ask;
    private double epsCurrentYear;
    private double epsNextYear;
    private double perCurrent;
    private double per5years;
    private double marketCapitalization;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date lastQuarterlyFigures;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date nextQuarterlyFigures;
    private StockIndex stockIndex;
    private double rateProgress6month;
    private double rateProgress1year;
    private List<Double> reversal3Month;
    private double profitGrowth;
    private double earningsRevision;
    private double analystEstimation;
    private int analystEstimationCount;
    private String name;
    private List<FundamentalDataUrl> urls = new ArrayList<>();
    private boolean automaticRating = true;

    private int overallRating;
    private int roeRating;
    private int ebitRating;
    private int equityRatioRating;
    private int perCurrentRating;
    private int per5yearsRating;
    private int analystEstimationRating;
    private int lastQuarterlyFiguresRating;
    private int rateProgress6monthRating;
    private int rateProgress1yearRating;
    private int rateMomentumRating;
    private int reversal3MonthRating;
    private int profitGrowthRating;
    private int earningsRevisionRating;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * get the symbol of the stock
     * @return symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * set the symbol of the stock
     * @param symbol symbol
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * get the business years of the share
     * @return the business years
     */
    public ArrayList<String> getBusinessYears() {
        return businessYears;
    }

    /**
     * set the business years of the share
     * @param businessYears businessYears
     */
    public void setBusinessYears(ArrayList<String> businessYears) {
        this.businessYears = businessYears;
    }

    /**
     * get the return on equity (Eigenkapitalrendite) of the share in percentage
     * @return the roe
     */
    public double getRoe() {
        return roe;
    }

    /**
     * set the return on equity (Eigenkapitalrendite) of the share in percentage
     * @param roe roe
     */
    public void setRoe(double roe) {
        this.roe = roe;
    }

    /**
     * get the ebit (EBIT-Marge) of the share in percentage
     * @return ebit
     */
    public double getEbit() {
        return ebit;
    }

    /**
     * set the ebit (EBIT-Marge) of the share in percentage
     * @param ebit ebit
     */
    public void setEbit(double ebit) {
        this.ebit = ebit;
    }

    /**
     * get the equity ratio (Eigenkapitalquote) of the share in percentage
     * @return equity ratio
     */
    public double getEquityRatio() {
        return equityRatio;
    }

    /**
     * set the equity ratio (Eigenkapitalquote) of the share in percentage
     * @param equityRatio equity ratio
     */
    public void setEquityRatio(double equityRatio) {
        this.equityRatio = equityRatio;
    }

    /**
     * get the current ask price of the share in percentage
     * @return ask price
     */
    public double getAsk() {
        return ask;
    }

    /**
     * set the current ask price of the share in percentage
     * @param ask ask price
     */
    public void setAsk(double ask) {
        this.ask = ask;
    }

    public double getEpsCurrentYear() {
        return epsCurrentYear;
    }

    public void setEpsCurrentYear(double epsCurrentYear) {
        this.epsCurrentYear = epsCurrentYear;
    }

    public double getEpsNextYear() {
        return epsNextYear;
    }

    public void setEpsNextYear(double epsNextYear) {
        this.epsNextYear = epsNextYear;
    }

    /**
     * get the current price-earning ratio PER (KGV)
     * @return current per
     */
    public double getPerCurrent() {
        return perCurrent;
    }

    /**
     * set the price-earning ratio PER (KGV)
     * @param perCurrent per Current
     */
    public void setPerCurrent(double perCurrent) {
        this.perCurrent = perCurrent;
    }

    /**
     * get the 5 years price-earning ratio PER (KGV)
     * @return 5 years per
     */
    public double getPer5years() {
        return per5years;
    }

    /**
     * set the 5 years price-earning ratio PER (KGV)
     * @param per5years 5 years per
     */
    public void setPer5years(double per5years) {
        this.per5years = per5years;
    }

    /**
     * get the String value of the next year
     * @return year
     */
    public String getNextYear() {
        if (businessYears.size() > 0) {
            return businessYears.get(0);
        }
        return "";
    }

    /**
     * get the String value of the current year
     * @return year
     */
    public String getCurrentYear() {
        if (businessYears.size() > 1) {
            return businessYears.get(1);
        }
        return "";
    }

    /**
     * get the String value of the last year
     * @return year
     */
    public String getLastYear() {
        if (businessYears.size() > 2) {
            return businessYears.get(2);
        }
        return "";
    }

    /**
     * get the String value of two years ago
     * @return year
     */
    public String getTwoYearsAgo() {
        if (businessYears.size() > 3) {
            return businessYears.get(3);
        }
        return "";
    }

    /**
     * get the String value of three years ago
     * @return year
     */
    public String getThreeYearsAgo() {
        if (businessYears.size() > 4) {
            return businessYears.get(4);
        }
        return "";
    }

    public int getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(int overallRating) {
        this.overallRating = overallRating;
    }

    public int getRoeRating() {
        return roeRating;
    }

    public void setRoeRating(int roeRating) {
        this.roeRating = roeRating;
    }

    public int getEbitRating() {
        return ebitRating;
    }

    public void setEbitRating(int ebitRating) {
        this.ebitRating = ebitRating;
    }

    public int getEquityRatioRating() {
        return equityRatioRating;
    }

    public void setEquityRatioRating(int equityRatioRating) {
        this.equityRatioRating = equityRatioRating;
    }

    public int getPerCurrentRating() {
        return perCurrentRating;
    }

    public void setPerCurrentRating(int perCurrentRating) {
        this.perCurrentRating = perCurrentRating;
    }

    public int getPer5yearsRating() {
        return per5yearsRating;
    }

    public void setPer5yearsRating(int per5yearsRating) {
        this.per5yearsRating = per5yearsRating;
    }

    public double getAnalystEstimation() {
        return analystEstimation;
    }

    public void setAnalystEstimation(double analystEstimation) {
        this.analystEstimation = analystEstimation;
    }

    public int getAnalystEstimationCount() {
        return analystEstimationCount;
    }

    public void setAnalystEstimationCount(int analystEstimationCount) {
        this.analystEstimationCount = analystEstimationCount;
    }

    public int getAnalystEstimationRating() {
        return analystEstimationRating;
    }

    public void setAnalystEstimationRating(int analystEstimationRating) {
        this.analystEstimationRating = analystEstimationRating;
    }

    public Date getLastQuarterlyFigures() {
        return lastQuarterlyFigures;
    }

    public void setLastQuarterlyFigures(Date lastQuarterlyFigures) {
        this.lastQuarterlyFigures = lastQuarterlyFigures;
    }

    public int getLastQuarterlyFiguresRating() {
        return lastQuarterlyFiguresRating;
    }

    public void setLastQuarterlyFiguresRating(int lastQuarterlyFiguresRating) {
        this.lastQuarterlyFiguresRating = lastQuarterlyFiguresRating;
    }

    public Date getNextQuarterlyFigures() {
        return nextQuarterlyFigures;
    }

    public void setNextQuarterlyFigures(Date nextQuarterlyFigures) {
        this.nextQuarterlyFigures = nextQuarterlyFigures;
    }

    public StockIndex getStockIndex() {
        return stockIndex;
    }

    public void setStockIndex(StockIndex stockIndex) {
        this.stockIndex = stockIndex;
    }

    public double getRateProgress6month() {
        return rateProgress6month;
    }

    public void setRateProgress6month(double rateProgress6month) {
        this.rateProgress6month = rateProgress6month;
    }

    public double getRateProgress1year() {
        return rateProgress1year;
    }

    public void setRateProgress1year(double rateProgress1year) {
        this.rateProgress1year = rateProgress1year;
    }

    public int getRateProgress6monthRating() {
        return rateProgress6monthRating;
    }

    public void setRateProgress6monthRating(int rateProgress6monthRating) {
        this.rateProgress6monthRating = rateProgress6monthRating;
    }

    public int getRateProgress1yearRating() {
        return rateProgress1yearRating;
    }

    public void setRateProgress1yearRating(int rateProgress1yearRating) {
        this.rateProgress1yearRating = rateProgress1yearRating;
    }

    public int getRateMomentumRating() {
        return rateMomentumRating;
    }

    public void setRateMomentumRating(int rateMomentumRating) {
        this.rateMomentumRating = rateMomentumRating;
    }

    public List<Double> getReversal3Month() {
        return reversal3Month;
    }

    public void setReversal3Month(List<Double> reversal3Month) {
        this.reversal3Month = reversal3Month;
    }

    public int getReversal3MonthRating() {
        return reversal3MonthRating;
    }

    public void setReversal3MonthRating(int reversal3MonthRating) {
        this.reversal3MonthRating = reversal3MonthRating;
    }

    public double getProfitGrowth() {
        return profitGrowth;
    }

    public void setProfitGrowth(double profitGrowth) {
        this.profitGrowth = profitGrowth;
    }

    public int getProfitGrowthRating() {
        return profitGrowthRating;
    }

    public void setProfitGrowthRating(int profitGrowthRating) {
        this.profitGrowthRating = profitGrowthRating;
    }

    public double getEarningsRevision() {
        return earningsRevision;
    }

    public void setEarningsRevision(double earningsRevision) {
        this.earningsRevision = earningsRevision;
    }

    public int getEarningsRevisionRating() {
        return earningsRevisionRating;
    }

    public void setEarningsRevisionRating(int earningsRevisionRating) {
        this.earningsRevisionRating = earningsRevisionRating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FundamentalDataUrl> getUrls() {
        return urls;
    }

    public void setUrls(List<FundamentalDataUrl> urls) {
        this.urls = urls;
    }

    public StockType getStockType() {
        return stockType;
    }

    public void setStockType(StockType stockType) {
        this.stockType = stockType;
    }

    public double getMarketCapitalization() {
        return marketCapitalization;
    }

    public void setMarketCapitalization(double marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
    }

    public boolean isAutomaticRating() {
        return automaticRating;
    }

    public void setAutomaticRating(boolean automaticRating) {
        this.automaticRating = automaticRating;
    }

    @Override
    public String toString() {
        return "FundamentalData{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", symbol='" + symbol + '\'' +
                ", businessYears=" + businessYears +
                ", roe=" + roe +
                ", ebit=" + ebit +
                ", equityRatio=" + equityRatio +
                ", ask=" + ask +
                ", perCurrent=" + perCurrent +
                ", per5years=" + per5years +
                '}';
    }
}
