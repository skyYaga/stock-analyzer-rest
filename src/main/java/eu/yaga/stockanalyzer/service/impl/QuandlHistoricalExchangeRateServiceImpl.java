package eu.yaga.stockanalyzer.service.impl;

import com.jimmoores.quandl.DataSetRequest;
import com.jimmoores.quandl.QuandlSession;
import com.jimmoores.quandl.Row;
import com.jimmoores.quandl.TabularResult;
import com.jimmoores.quandl.util.QuandlRuntimeException;
import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.model.RateProgressBean;
import eu.yaga.stockanalyzer.model.StockIndex;
import eu.yaga.stockanalyzer.model.historicaldata.HistoricalDataQuote;
import eu.yaga.stockanalyzer.service.HistoricalExchangeRateService;
import eu.yaga.stockanalyzer.util.HttpHelper;
import eu.yaga.stockanalyzer.util.QuandlCode;
import eu.yaga.stockanalyzer.util.QuandlProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of the {@link HistoricalExchangeRateService}
 */
public class QuandlHistoricalExchangeRateServiceImpl implements HistoricalExchangeRateService {

    @Autowired
    private QuandlProperties quandlProperties;

    private static final Logger log = LoggerFactory.getLogger(QuandlHistoricalExchangeRateServiceImpl.class);

    // Quandl Frankfurt Stock Exchange / XETRA
    private static final String FSE_PREFIX = "FSE/";
    private static final String FSE_POSTFIX = "_X";
    // Quandl Stuttgart Stock Exchange
    private static final String SSE_PREFIX = "SSE/";
    // Quandl US companies
    private static final String US_PREFIX = "WIKI/";
    // Quandl Google finance
    private static final String GOOG_FRA_PREFIX = "GOOG/FRA_";
    private static final String GOOG_NASDAQ_PREFIX = "GOOG/NASDAQ_";

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter dtfGermany = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * This method returns historical exchange Rates of the given stock
     *
     * @param symbol Symbol of the stock
     * @param dateStringFrom   Date of the start of the historical data (yyyy-MM-dd)
     * @param dateStringTo     Date of the end of the historical data (yyyy-MM-dd)
     * @return Historical Exchange Rates
     */
    @Override
    public List<HistoricalDataQuote> getHistoricalExchangeRates(String symbol, String dateStringFrom, String dateStringTo) throws ParseException {
        log.info("Getting HistoricalExchangeRates for: " + symbol + " " + dateStringFrom + " " + dateStringTo);
        String[] splitSymbol = symbol.split("\\.");
        String cleanSymbol = splitSymbol[0];
        String exchange = "";
        if (splitSymbol.length > 1) {
            exchange = splitSymbol[1];
        }

        List<QuandlCode> quandlCodeList = buildQuandlCode(cleanSymbol, exchange);

        log.info("Clean Symbol: " + cleanSymbol);

        LocalDate dateTo = LocalDate.now();
        if (dateStringTo != null) {
            dateTo = LocalDate.parse(dateStringTo, DateTimeFormatter.ISO_LOCAL_DATE);
        }

        LocalDate dateFrom = LocalDate.now().minusYears(1);
        if (dateStringFrom != null) {
            dateFrom = LocalDate.parse(dateStringFrom, DateTimeFormatter.ISO_LOCAL_DATE);
        }

        if (dateFrom.equals(dateTo)) {
            throw new RuntimeException("The dates may not be equal!");
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new RuntimeException("The from date has to be before the to date!");
        }

        List<HistoricalDataQuote> quoteList = new ArrayList<>();
        QuandlSession session = QuandlSession.create(quandlProperties.getAuth().getToken());
        for (QuandlCode quandlCode : quandlCodeList) {
            log.info("Querying: " + quandlCode.getCode() + " from: " + dateFrom + " to: " + dateTo);
            try {
                TabularResult tabularResult = session.getDataSet(
                        DataSetRequest.Builder
                                .of(quandlCode.getCode())
                                .withStartDate(dateFrom)
                                .withEndDate(dateTo)
                                .build());

                log.info(tabularResult.toPrettyPrintedString());

                for (Row row : tabularResult) {
                    String dateString = row.getString("Date");
                    double close = row.getDouble(quandlCode.getCloseColumnName());
                    quoteList.add(new HistoricalDataQuote(symbol, dateString, close));
                }
                break;
            } catch (QuandlRuntimeException e) {
                log.warn("Error requesting quandl code: " + quandlCode.getCode(), e);
            }
        }

        return quoteList;
    }

    /**
     * Generates a list of quandlCodes to try
     * @param cleanSymbol the stocks symbol
     * @param exchange the symbol of the exchange
     * @return a list of Strings with quandl codes
     */
    private List<QuandlCode> buildQuandlCode(String cleanSymbol, String exchange) {

        List<QuandlCode> quandlCodeList = new ArrayList<>();

        switch (exchange) {
            case "F":
            case "DE":
                quandlCodeList.add(new QuandlCode(GOOG_FRA_PREFIX + cleanSymbol, "EUR", "Close"));
                quandlCodeList.add(new QuandlCode(FSE_PREFIX + cleanSymbol + FSE_POSTFIX, "EUR", "Close"));
                quandlCodeList.add(new QuandlCode(SSE_PREFIX + cleanSymbol, "EUR", "Last"));
                break;
            case "US":
                quandlCodeList.add(new QuandlCode(US_PREFIX + cleanSymbol, "USD", "Close"));
                quandlCodeList.add(new QuandlCode(GOOG_NASDAQ_PREFIX + cleanSymbol, "USD", "Close"));
                break;
            default:
                quandlCodeList.add(new QuandlCode(US_PREFIX + cleanSymbol, "USD", "Close"));
                quandlCodeList.add(new QuandlCode(GOOG_FRA_PREFIX + cleanSymbol, "EUR", "Close"));
                quandlCodeList.add(new QuandlCode(GOOG_NASDAQ_PREFIX + cleanSymbol, "USD", "Close"));
        }

        return quandlCodeList;
    }

    /**
     * This method returns the stock's reaction to quarterly figures (comparing it to its index)
     *
     * @param fundamentalData of the stock
     * @return the progress difference to the index
     */
    @Override
    public double getReactionToQuarterlyFigures(FundamentalData fundamentalData) {
        try {
            Date dateLegacy = fundamentalData.getLastQuarterlyFigures();
            String symbol = fundamentalData.getSymbol();
            String indexSymbol = fundamentalData.getStockIndex().getSymbol();

            if (dateLegacy == null || symbol == null || indexSymbol == null) {
                return -9999;
            }

            LocalDate date = DateTimeUtils.toInstant(dateLegacy).atZone(ZoneId.systemDefault()).toLocalDate();

            String dateString = date.format(dtf);
            String priorDay = date.minusDays(1).format(dtf);

            List<HistoricalDataQuote> ratesSymbol = getHistoricalExchangeRates(symbol, priorDay, dateString);
            LocalDate dateTmp = date;

            int cnt = 0;
            while (ratesSymbol.size() < 2 && cnt <= 7) {
                cnt++;
                dateTmp = dateTmp.minusDays(1);
                priorDay = dateTmp.format(dtf);
                ratesSymbol = getHistoricalExchangeRates(symbol, priorDay, dateString);
            }

            if (ratesSymbol.size() < 2) {
                throw new RuntimeException("Unable to get historical exchange rates for " + symbol);
            }

            //List<HistoricalDataQuote> ratesIndex = getHistoricalExchangeRates(indexSymbol, priorDay, dateString);
            //if (ratesIndex.size() == 0) {
            List<HistoricalDataQuote> ratesIndex = getRatesIndexFromBackupProvider(fundamentalData.getStockIndex(), ratesSymbol);
            //}

            // calculate Data
            double closeSymbol = ratesSymbol.get(0).getClose();
            double closeSymbolPriorDay = ratesSymbol.get(1).getClose();
            double closeIndex = ratesIndex.get(0).getClose();
            double closeIndexPriorDay = ratesIndex.get(1).getClose();

            double progressSymbol = (1 - closeSymbolPriorDay / closeSymbol) * 100;
            log.info("progressSymbol " + symbol + ": " + progressSymbol);
            double progressIndex = (1 - closeIndexPriorDay / closeIndex) * 100;
            log.info("progressIndex " + indexSymbol + ": " + progressIndex);

            double totalProgress = progressSymbol - progressIndex;
            log.info("totalProgress: " + totalProgress);
            return totalProgress;
        } catch (ParseException e) {
            return -9999;
        }
    }


    /**
     * This method calculates the stock progression within the last 6 months
     *
     * @param fundamentalData of the stock
     * @return the progression in percent
     */
    @Override
    public double getRateProgress6month(FundamentalData fundamentalData) {
        log.info("getRateProgress6month");
        return getRateProgress(fundamentalData.getSymbol(), 6, ChronoUnit.MONTHS).getProgress();
    }

    /**
     * This method calculates the stock progression within the last 1 year
     *
     * @param fundamentalData of the stock
     * @return the progression in percent
     */
    @Override
    public double getRateProgress1year(FundamentalData fundamentalData) {
        log.info("getRateProgress1year");
        return getRateProgress(fundamentalData.getSymbol(), 1, ChronoUnit.YEARS).getProgress();
    }

    /**
     * This method calculates the stock progression compared to its index of the last 3 months
     *
     * @param fundamentalData of the stock
     * @return a list with the progression of the last 3 months
     */
    @Override
    public List<Double> getReversal3Month(FundamentalData fundamentalData) {
        log.info("getReversal3Month");
        String symbol = fundamentalData.getSymbol();
        String stockIndex = fundamentalData.getStockIndex().getSymbol();

        if (symbol != null && stockIndex != null) {
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            LocalDate twoMonthAgo = LocalDate.now().minusMonths(2);
            LocalDate threeMonthAgo = LocalDate.now().minusMonths(3);
            LocalDate fourMonthAgo = LocalDate.now().minusMonths(4);

            LocalDate endOfLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
            LocalDate endOfTwoMonthAgo = twoMonthAgo.withDayOfMonth(twoMonthAgo.lengthOfMonth());
            LocalDate endOfThreeMonthAgo = threeMonthAgo.withDayOfMonth(threeMonthAgo.lengthOfMonth());
            LocalDate endOfFourMonthAgo = fourMonthAgo.withDayOfMonth(fourMonthAgo.lengthOfMonth());

            RateProgressBean symbolProgressLastMonthBean = getRateProgress(symbol, endOfLastMonth, endOfTwoMonthAgo);
            RateProgressBean symbolProgressTwoMonthAgoBean = getRateProgress(symbol, endOfTwoMonthAgo, endOfThreeMonthAgo);
            RateProgressBean symbolProgressThreeMonthAgoBean = getRateProgress(symbol, endOfThreeMonthAgo, endOfFourMonthAgo);

            double symbolProgressLastMonth = symbolProgressLastMonthBean.getProgress();
            double symbolProgressTwoMonthAgo = symbolProgressTwoMonthAgoBean.getProgress();
            double symbolProgressThreeMonthAgo = symbolProgressThreeMonthAgoBean.getProgress();

            double indexProgressLastMonth = getIndexRateProgress(
                    fundamentalData.getStockIndex(),
                    symbolProgressLastMonthBean.getBaseDateQuote().getDate(),
                    symbolProgressLastMonthBean.getCompareDateQuote().getDate());
            double indexProgressTwoMonthAgo = getIndexRateProgress(
                    fundamentalData.getStockIndex(),
                    symbolProgressTwoMonthAgoBean.getBaseDateQuote().getDate(),
                    symbolProgressTwoMonthAgoBean.getCompareDateQuote().getDate());
            double indexProgressThreeMonthAgo = getIndexRateProgress(
                    fundamentalData.getStockIndex(),
                    symbolProgressThreeMonthAgoBean.getBaseDateQuote().getDate(),
                    symbolProgressThreeMonthAgoBean.getCompareDateQuote().getDate());

            List<Double> reversalList = new ArrayList<>();
            reversalList.add(symbolProgressLastMonth - indexProgressLastMonth);
            reversalList.add(symbolProgressTwoMonthAgo - indexProgressTwoMonthAgo);
            reversalList.add(symbolProgressThreeMonthAgo - indexProgressThreeMonthAgo);

            return reversalList;
        } else {
            List<Double> reversalList = new ArrayList<>();
            reversalList.add(-999.0);
            return reversalList;
        }

    }

    /**
     * returns the rate progress from today
     * @param symbol the symbol
     * @param amount the amount of units to be subtracted
     * @param chronoUnit the unit of the amount (days, months, ...)
     * @return the rate progress
     */
    private RateProgressBean getRateProgress(String symbol, int amount, ChronoUnit chronoUnit) {
        LocalDate today = LocalDate.now();
        LocalDate compareDate = today.minus(amount, chronoUnit);
        return getRateProgress(symbol, today, compareDate);
    }

    /**
     * returns the rate progress from the base date
     * @param symbol the symbol
     * @param baseDate the reference date
     * @param compareDate the date to compare with
     * @return the rate progress
     */
    private RateProgressBean getRateProgress(String symbol, LocalDate baseDate, LocalDate compareDate) {
        try {
            if (symbol != null && baseDate != null && compareDate != null) {
                // Fetch data
                LocalDate baseDateMinus = baseDate.minusDays(1);

                List<HistoricalDataQuote> ratesToday = getHistoricalExchangeRates(symbol, baseDateMinus.format(dtf), baseDate.format(dtf));
                int cnt = 0;
                while (ratesToday.size() < 1 && cnt <= 10) {
                    cnt++;
                    baseDateMinus = baseDateMinus.minusDays(1);
                    ratesToday = getHistoricalExchangeRates(symbol, baseDateMinus.format(dtf), baseDate.format(dtf));
                }

                // Find data for compareDate
                LocalDate compareDateMinus = compareDate.minusDays(1);

                List<HistoricalDataQuote> ratesCompareDate = getHistoricalExchangeRates(symbol, compareDateMinus.format(dtf), compareDate.format(dtf));
                cnt = 0;
                while (ratesCompareDate.size() < 1 && cnt <= 10) {
                    cnt++;
                    compareDateMinus = compareDateMinus.minusDays(1);
                    ratesCompareDate = getHistoricalExchangeRates(symbol, compareDateMinus.format(dtf), compareDate.format(dtf));
                }

                double closeToday = ratesToday.get(0).getClose();
                log.info("closeToday: " + closeToday);
                double closeCompareDate = ratesCompareDate.get(0).getClose();
                log.info("closeCompareDate: " + closeCompareDate);

                double rateProgress = (closeToday - closeCompareDate) / closeCompareDate * 100;
                log.info("rateProgress: " + rateProgress);

                return new RateProgressBean(ratesToday.get(0), ratesCompareDate.get(0), rateProgress);
            }
            return null;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * returns the index' rate progress from the base date with backup data source
     * @param index the StockIndex
     * @param baseDateString the reference date
     * @param compareDateString the date to compare with
     * @return the rate progress
     */
    private double getIndexRateProgress(StockIndex index, String baseDateString, String compareDateString) {
        if (index != null && baseDateString != null && compareDateString != null) {
            LocalDate baseDate = LocalDate.parse(baseDateString, dtf);
            LocalDate compareDate = LocalDate.parse(compareDateString, dtf);

            HistoricalDataQuote ratesToday = getRatesIndexFromBackupProvider(index, baseDate);
            HistoricalDataQuote ratesCompareDate = getRatesIndexFromBackupProvider(index, compareDate);

            double closeToday = ratesToday.getClose();
            log.info("closeToday: " + closeToday);
            double closeCompareDate = ratesCompareDate.getClose();
            log.info("closeCompareDate: " + closeCompareDate);

            double rateProgress = (closeToday - closeCompareDate) / closeCompareDate * 100;
            log.info("rateProgress: " + rateProgress);

            return rateProgress;
        }
        return -9999;
    }

    private List<HistoricalDataQuote> getRatesIndexFromBackupProvider(StockIndex stockIndex, List<HistoricalDataQuote> compareStock) {
        List<HistoricalDataQuote> quotes = new ArrayList<>();

        for (HistoricalDataQuote compareQuote : compareStock) {
            quotes.add(getRatesIndexFromBackupProvider(stockIndex, LocalDate.parse(compareQuote.getDate(), dtf)));
        }

        return quotes;
    }

    private HistoricalDataQuote getRatesIndexFromBackupProvider(StockIndex index, LocalDate date) {
        HistoricalDataQuote quote = new HistoricalDataQuote();

        try {
            String dateString = dtf.format(date);
            String dateGermanString = dtfGermany.format(date);

            URL idxUrl = new URL(index.getOnvistaApiUrl() + dateGermanString);
            String resultJson = HttpHelper.queryHTML(idxUrl);
            JSONObject jsonObject = new JSONObject(resultJson);

            NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
            Number number = format.parse(jsonObject.getString("close"));

            quote.setSymbol(index.getSymbol());
            quote.setDate(dateString);
            quote.setClose(number.doubleValue());

        } catch (MalformedURLException | ParseException e) {
            log.error(e.getLocalizedMessage());
        }

        return quote;
    }
}
