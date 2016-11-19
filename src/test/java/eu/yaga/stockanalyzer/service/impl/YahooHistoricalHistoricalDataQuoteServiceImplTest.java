package eu.yaga.stockanalyzer.service.impl;

import eu.yaga.stockanalyzer.Application;
import eu.yaga.stockanalyzer.model.historicaldata.HistoricalDataQuery;
import eu.yaga.stockanalyzer.model.historicaldata.HistoricalDataQuote;
import eu.yaga.stockanalyzer.model.historicaldata.HistoricalDataResults;
import eu.yaga.stockanalyzer.model.historicaldata.YqlHistoricalDataQuery;
import eu.yaga.stockanalyzer.service.HistoricalExchangeRateService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link YahooHistoricalExchangeRateServiceImpl}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class YahooHistoricalHistoricalDataQuoteServiceImplTest {

    private String symbol = "AAPL";
    private static final String YQL_BASE_URL = YahooHistoricalExchangeRateServiceImpl.YQL_BASE_URL;
    private static final String YQL_QUERY_POSTFIX = YahooHistoricalExchangeRateServiceImpl.YQL_QUERY_POSTFIX;
    private static final String YQL_QUERY_HISTORICAL_RATES = YahooHistoricalExchangeRateServiceImpl.YQL_QUERY_HISTORICAL_RATES;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private Calendar calendar = GregorianCalendar.getInstance();

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HistoricalExchangeRateService service = new YahooHistoricalExchangeRateServiceImpl();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHistoricalExchangeRatesNoOptionalValues() throws Exception {
        Date dateTo = new Date();
        calendar.setTime(dateTo);
        calendar.add(Calendar.YEAR, -1);
        Date dateFrom = calendar.getTime();

        when(restTemplate.getForObject(
                eq(YQL_BASE_URL + String.format(YQL_QUERY_HISTORICAL_RATES, symbol, sdf.format(dateFrom), sdf.format(dateTo)) + YQL_QUERY_POSTFIX),
                eq(YqlHistoricalDataQuery.class))).thenReturn(generateMock(symbol, sdf.format(dateFrom), sdf.format(dateTo)));

        List<HistoricalDataQuote> erList = service.getHistoricalExchangeRates(symbol, null, null);

        assertTrue("erList contains data for one year", erList.size() >= 365 && erList.size() <= 367);
        assertEquals(sdf.format(dateFrom), erList.get(0).getDate());
        assertEquals(symbol, erList.get(0).getSymbol());
        assertEquals(sdf.format(dateTo), erList.get(erList.size() - 1).getDate());
    }

    @Test
    public void testHistoricalExchangeRatesFromValueOnly() throws Exception {
        Date dateTo = new Date();
        calendar.setTime(dateTo);
        calendar.add(Calendar.DAY_OF_MONTH, -20);
        Date dateFrom = calendar.getTime();

        when(restTemplate.getForObject(
                eq(YQL_BASE_URL + String.format(YQL_QUERY_HISTORICAL_RATES, symbol, sdf.format(dateFrom), sdf.format(dateTo)) + YQL_QUERY_POSTFIX),
                eq(YqlHistoricalDataQuery.class))).thenReturn(generateMock(symbol, sdf.format(dateFrom), sdf.format(dateTo)));

        List<HistoricalDataQuote> erList = service.getHistoricalExchangeRates(symbol, sdf.format(dateFrom), null);

        assertEquals(21, erList.size());
        assertEquals(sdf.format(dateFrom), erList.get(0).getDate());
        assertEquals(symbol, erList.get(0).getSymbol());
        assertEquals(sdf.format(dateTo), erList.get(erList.size() - 1).getDate());
    }

    @Test
    public void testHistoricalExchangeRatesToValueOnly() throws Exception {
        String dateToString = "2016-01-01";
        calendar.setTime(sdf.parse(dateToString));
        calendar.add(Calendar.YEAR, -1);
        Date dateFrom = calendar.getTime();

        when(restTemplate.getForObject(
                eq(YQL_BASE_URL + String.format(YQL_QUERY_HISTORICAL_RATES, symbol, sdf.format(dateFrom), dateToString) + YQL_QUERY_POSTFIX),
                eq(YqlHistoricalDataQuery.class))).thenReturn(generateMock(symbol, sdf.format(dateFrom), dateToString));

        List<HistoricalDataQuote> erList = service.getHistoricalExchangeRates(symbol, null, dateToString);

        assertTrue("erList contains data for one year", erList.size() >= 365 && erList.size() <= 367);
        assertEquals(sdf.format(dateFrom), erList.get(0).getDate());
        assertEquals(symbol, erList.get(0).getSymbol());
        assertEquals(dateToString, erList.get(erList.size() - 1).getDate());
    }

    @Test
    public void testHistoricalExchangeRates() throws Exception {
        String from = "2016-01-01";
        String to = "2016-01-31";
        when(restTemplate.getForObject(
                eq(YQL_BASE_URL + String.format(YQL_QUERY_HISTORICAL_RATES, symbol, from, to) + YQL_QUERY_POSTFIX),
                eq(YqlHistoricalDataQuery.class))).thenReturn(generateMock(symbol, from, to));

        List<HistoricalDataQuote> erList = service.getHistoricalExchangeRates(symbol, from, to);

        assertEquals(31, erList.size());
        assertEquals(from, erList.get(0).getDate());
        assertEquals(symbol, erList.get(0).getSymbol());
        assertEquals(to, erList.get(30).getDate());
    }

    @Test(expected = RuntimeException.class)
    public void testHistoricalExchangeRatesWrongOrderException() throws Exception {
        String from = "2016-01-31";
        String to = "2016-01-01";

        service.getHistoricalExchangeRates(symbol, from, to);
    }

    @Test(expected = RuntimeException.class)
    public void testHistoricalExchangeRatesSameDateException() throws Exception {
        String from = "2016-01-31";
        String to = "2016-01-31";

        service.getHistoricalExchangeRates(symbol, from, to);
    }

    private YqlHistoricalDataQuery generateMock(String symbol, String fromString, String toString) throws Exception {
        YqlHistoricalDataQuery yqlQuery = new YqlHistoricalDataQuery();
        HistoricalDataQuery query = new HistoricalDataQuery();
        HistoricalDataResults results = new HistoricalDataResults();
        List<HistoricalDataQuote> quoteList = new ArrayList<>();

        Date currentDate = sdf.parse(fromString);
        if (currentDate.before(sdf.parse(toString))) {
            // This is the "normal" case
            while (!currentDate.after(sdf.parse(toString))) {
                quoteList.add(new HistoricalDataQuote(symbol, sdf.format(currentDate), generateRandomDouble()));
                calendar.setTime(currentDate);
                calendar.add(Calendar.DATE, 1);
                currentDate = calendar.getTime();
            }
        } else {
            return null;
        }

        results.setQuote(quoteList);
        query.setResults(results);
        yqlQuery.setQuery(query);

        return yqlQuery;
    }

    private double generateRandomDouble() {
        Random r = new Random();
        double rangeMin = 100;
        double rangeMax = 200;

        return rangeMin + (rangeMax - rangeMin) * r.nextDouble();
    }

}
