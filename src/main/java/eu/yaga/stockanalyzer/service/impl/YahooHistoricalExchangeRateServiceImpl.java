package eu.yaga.stockanalyzer.service.impl;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.model.RateProgressBean;
import eu.yaga.stockanalyzer.model.StockIndex;
import eu.yaga.stockanalyzer.model.historicaldata.HistoricalDataQuote;
import eu.yaga.stockanalyzer.model.historicaldata.YqlHistoricalDataQuery;
import eu.yaga.stockanalyzer.service.HistoricalExchangeRateService;
import eu.yaga.stockanalyzer.util.HttpHelper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Implementation of the {@link HistoricalExchangeRateService}
 */
public class YahooHistoricalExchangeRateServiceImpl implements HistoricalExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(YahooHistoricalExchangeRateServiceImpl.class);

    static final String YQL_BASE_URL = "https://query.yahooapis.com/v1/public/yql";
    static final String YQL_QUERY_POSTFIX = "&format=json&env=store://datatables.org/alltableswithkeys";
    static final String YQL_QUERY_HISTORICAL_RATES =
            "?q=select * from yahoo.finance.historicaldata where symbol = '%s' and startDate = '%s' and endDate = '%s'";


    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter dtfGermany = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired
    private RestTemplate restTemplate;

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
        Calendar calendar = GregorianCalendar.getInstance();

        // Create default values
        Date dateTo = new Date();
        if (dateStringTo != null) {
            dateTo = sdf.parse(dateStringTo);
        }

        calendar.setTime(dateTo);
        calendar.add(Calendar.YEAR, -1);
        Date dateFrom = calendar.getTime();

        if (dateStringFrom != null) {
            dateFrom = sdf.parse(dateStringFrom);
        }

        if (dateFrom.equals(dateTo)) {
            throw new RuntimeException("The dates may not be equal!");
        }
        if (dateFrom.after(dateTo)) {
            throw new RuntimeException("The from date has to be before the to date!");
        }

        String queryString = String.format(YQL_QUERY_HISTORICAL_RATES, symbol, sdf.format(dateFrom), sdf.format(dateTo));

        YqlHistoricalDataQuery queryResult = new YqlHistoricalDataQuery();
        try {
            queryResult = restTemplate.getForObject(YQL_BASE_URL + queryString + YQL_QUERY_POSTFIX, YqlHistoricalDataQuery.class);
        } catch (Exception e) {
            log.error("YqlHistoricalDataQuery failed: " + e.getLocalizedMessage());
        }
        log.info(queryResult.toString());

        if (queryResult.getQuery() != null
                && queryResult.getQuery().getResults() != null
                && queryResult.getQuery().getResults().getQuote() != null) {
            return queryResult.getQuery().getResults().getQuote();
        }

        return new ArrayList<>();
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

            LocalDate date = dateLegacy.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            String dateString = date.format(dtf);
            String priorDay = date.minusDays(1).format(dtf);

            List<HistoricalDataQuote> ratesSymbol = getHistoricalExchangeRates(symbol, priorDay, dateString);
            LocalDate dateTmp = date;
            int cnt = 0;
            while (ratesSymbol.size() < 2 && cnt < 10) {
                dateTmp = dateTmp.minusDays(1);
                priorDay = dateTmp.format(dtf);
                ratesSymbol = getHistoricalExchangeRates(symbol, priorDay, dateString);
                cnt ++;
            }

            List<HistoricalDataQuote> ratesIndex = getHistoricalExchangeRates(indexSymbol, priorDay, dateString);
            if (ratesIndex.size() == 0) {
                ratesIndex = getRatesIndexFromBackupProvider(fundamentalData.getStockIndex(), ratesSymbol);
            }

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
                while (ratesToday.size() < 1 && cnt < 10) {
                    baseDateMinus = baseDateMinus.minusDays(1);
                    ratesToday = getHistoricalExchangeRates(symbol, baseDateMinus.format(dtf), baseDate.format(dtf));
                    cnt++;
                }

                // Find data for compareDate
                LocalDate compareDateMinus = compareDate.minusDays(1);

                List<HistoricalDataQuote> ratesCompareDate = getHistoricalExchangeRates(symbol, compareDateMinus.format(dtf), compareDate.format(dtf));
                cnt = 0;
                while (ratesCompareDate.size() < 1 && cnt < 10) {
                    compareDateMinus = compareDateMinus.minusDays(1);
                    ratesCompareDate = getHistoricalExchangeRates(symbol, compareDateMinus.format(dtf), compareDate.format(dtf));
                    cnt++;
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
        try {
            if (index != null && baseDateString != null && compareDateString != null) {
                LocalDate baseDate = LocalDate.parse(baseDateString, dtf);
                LocalDate baseDateMinus = baseDate.minusDays(1);
                LocalDate compareDate = LocalDate.parse(compareDateString, dtf);
                LocalDate compareDateMinus = compareDate.minusDays(1);

                List<HistoricalDataQuote> ratesToday = getHistoricalExchangeRates(index.getSymbol(), baseDateMinus.format(dtf), baseDate.format(dtf));
                List<HistoricalDataQuote> ratesCompareDate = getHistoricalExchangeRates(index.getSymbol(), compareDateMinus.format(dtf), compareDate.format(dtf));

                if (ratesToday.size() < 1 || ratesCompareDate.size() < 1) {
                    ratesToday.add(getRatesIndexFromBackupProvider(index, baseDate));
                    ratesCompareDate.add(getRatesIndexFromBackupProvider(index, compareDate));
                }

                double closeToday = ratesToday.get(0).getClose();
                log.info("closeToday: " + closeToday);
                double closeCompareDate = ratesCompareDate.get(0).getClose();
                log.info("closeCompareDate: " + closeCompareDate);

                double rateProgress = (closeToday - closeCompareDate) / closeCompareDate * 100;
                log.info("rateProgress: " + rateProgress);

                return rateProgress;
            }
            return -9999;
        } catch (ParseException e) {
            return -9999;
        }
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

        } catch (MalformedURLException e) {
            log.error(e.getLocalizedMessage());
        } catch (ParseException e) {
            log.error(e.getLocalizedMessage());
        }

        return quote;
    }
}
