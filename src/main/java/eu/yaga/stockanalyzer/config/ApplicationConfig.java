package eu.yaga.stockanalyzer.config;

import eu.yaga.stockanalyzer.parser.OnVistaParser;
import eu.yaga.stockanalyzer.service.*;
import eu.yaga.stockanalyzer.service.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring Application Config
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public HistoricalExchangeRateService getHistoricalExchangeRateService() {
        return new YahooHistoricalExchangeRateServiceImpl();
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
        return new RestTemplate();
    }

    @Bean
    public EmailService getEmailService() {
        return new MailjetEmailServiceImpl();
    }

}
