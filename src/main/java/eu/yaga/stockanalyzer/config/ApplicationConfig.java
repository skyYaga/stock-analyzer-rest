package eu.yaga.stockanalyzer.config;

import eu.yaga.stockanalyzer.parser.OnVistaParser;
import eu.yaga.stockanalyzer.service.*;
import eu.yaga.stockanalyzer.service.impl.*;
import eu.yaga.stockanalyzer.util.XUserAgentInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Spring Application Config
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public HistoricalExchangeRateService getHistoricalExchangeRateService() {
        //return new YahooHistoricalExchangeRateServiceImpl();
        return new QuandlHistoricalExchangeRateServiceImpl();
    }

    @Bean
    public FundamentalDataService getFundamentalDataService() {
        return new OnVistaFundamentalDataServiceImpl();
    }

    @Bean
    public EarningsRevisionService getEarningsRevisionService() {
        return new FinanzenNetEarningsRevisionServiceImpl();
    }

    @Bean
    public AnalystEstimationService getAnalystEstimationService() {
        return new AnalystEstimationImpl();
    }

    @Bean
    public CurrentStockQuotesService getCurrentStockQuotesService() {
        return new YahooCurrentStockQuotesServiceImpl();
    }

    @Autowired
    private HistoricalExchangeRateService historicalExchangeRateService;

    @Bean
    public StockRatingBusinessService getStockRatingBusinessService() { return new StockRatingBusinessServiceImpl(historicalExchangeRateService); }

    @Bean
    public OnVistaParser getOnVistaParser() {
        return new OnVistaParser();
    }

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new XUserAgentInterceptor()));
        return restTemplate;
    }

    @Bean
    public EmailService getEmailService() {
        return new MailjetEmailServiceImpl();
    }

}
