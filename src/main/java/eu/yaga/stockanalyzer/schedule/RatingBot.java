package eu.yaga.stockanalyzer.schedule;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.repository.FundamentalDataRepository;
import eu.yaga.stockanalyzer.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Scheduled Ratings
 */
@Component
class RatingBot {

    private static final Logger log = LoggerFactory.getLogger(RatingBot.class);
    private static final String API_URL = "http://localhost:8081/api/";

    @Autowired
    private EmailService emailService;

    @Autowired
    private FundamentalDataRepository fundamentalDataRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(cron = "0 0/10 8-20 * * *")
    void rateStocks() {
        log.info("Looking for stocks to rate...");

        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date oneWeekAgo = cal.getTime();

        List<FundamentalData> stocks = fundamentalDataRepository.findAll();

        int cnt = 0;

        for (FundamentalData stock : stocks) {
            if (cnt >= 5) {
                // request limiting
                break;
            }

            boolean ratingRequired = false;
            if (stock.getDate().before(oneWeekAgo)) {
                ratingRequired = true;
            }

            if (stock.getDate().after(oneWeekAgo) && stock.getLastQuarterlyFigures().after(stock.getDate())) {
                ratingRequired = true;
            }

            if (ratingRequired) {
                cnt++;
                log.info("Rating " + stock.getName());
                final int oldRating = stock.getOverallRating();

                FundamentalData fundamentalData = restTemplate.getForObject(API_URL + "fundamental-data/" + stock.getSymbol() + "/refresh", FundamentalData.class);
                final int newRating = fundamentalData.getOverallRating();

                log.info(stock.getName() + " - old rating: " + oldRating + " new rating: " + newRating);

                if (oldRating != newRating) {
                    log.info("Sending email...");
                    emailService.send("Neues Rating: " + stock.getName() + " (" + stock.getSymbol() +")",
                            "Für " + stock.getName() + " gibt es ein neues Rating: " + newRating + " (" + oldRating + ")");
                }
            }
        }
    }
}
