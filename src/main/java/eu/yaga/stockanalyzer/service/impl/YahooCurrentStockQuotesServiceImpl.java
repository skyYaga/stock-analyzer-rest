package eu.yaga.stockanalyzer.service.impl;

import eu.yaga.stockanalyzer.model.quotes.QuotesRate;
import eu.yaga.stockanalyzer.model.quotes.YqlQuotesQuery;
import eu.yaga.stockanalyzer.model.xchange.YqlXchangeQuery;
import eu.yaga.stockanalyzer.service.CurrentStockQuotesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of the {@link CurrentStockQuotesService}
 */
public class YahooCurrentStockQuotesServiceImpl implements CurrentStockQuotesService {

    private static final Logger log = LoggerFactory.getLogger(YahooCurrentStockQuotesServiceImpl.class);

    private static final String YQL_BASE_URL = "https://query.yahooapis.com/v1/public/yql";
    private static final String YQL_QUERY_POSTFIX = "&format=json&env=store://datatables.org/alltableswithkeys";
    private static final String YQL_QUERY_QUOTE = "?q=select * from yahoo.finance.quotes where symbol = '%s'";
    private static final String YQL_QUERY_EXCHANGE = "?q=select * from yahoo.finance.xchange where pair = '%sEUR'";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * This method returns the current rate of the given stock
     *
     * @param symbol Symbol of the stock
     * @return rate
     */
    @Override
    public double getCurrentRate(String symbol) {
        String queryString = String.format(YQL_QUERY_QUOTE, symbol);

        log.info("Querying " + YQL_BASE_URL + queryString + YQL_QUERY_POSTFIX);
        YqlQuotesQuery queryResult = restTemplate.getForObject(YQL_BASE_URL + queryString + YQL_QUERY_POSTFIX, YqlQuotesQuery.class);
        log.info(queryResult.toString());


        QuotesRate quote = queryResult.getQuery().getResults().getQuote();

        double ask = quote.getAsk();
        String currency = quote.getCurrency();

        if (!currency.equals("EUR")) {
            String xchangeQueryString = String.format(YQL_QUERY_EXCHANGE, currency);
            log.info("Querying exchange " + YQL_BASE_URL + xchangeQueryString + YQL_QUERY_POSTFIX);
            YqlXchangeQuery xchangeQueryResult = restTemplate.getForObject(YQL_BASE_URL + xchangeQueryString + YQL_QUERY_POSTFIX, YqlXchangeQuery.class);
            log.info(xchangeQueryResult.toString());

            double exchangeRate = xchangeQueryResult.getQuery().getResults().getRate().getRate();
            ask = ask * exchangeRate;
        }

        return ask;
    }
}
