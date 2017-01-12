package eu.yaga.stockanalyzer.schedule;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.model.StockType;
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

            if (stock.getDate() == null) {
                continue;
            }

            boolean ratingRequired = false;
            if (stock.getDate().before(oneWeekAgo)) {
                ratingRequired = true;
            }

            if (stock.getDate().after(oneWeekAgo) && stock.getLastQuarterlyFigures().after(stock.getDate())) {
                ratingRequired = true;
            }

            if (ratingRequired && stock.isAutomaticRating()) {
                cnt++;
                log.info("Rating " + stock.getName());
                final int oldRating = stock.getOverallRating();

                try {
                    FundamentalData fundamentalData = restTemplate.getForObject(API_URL + "fundamental-data/" + stock.getSymbol() + "/refresh", FundamentalData.class);
                    final int newRating = fundamentalData.getOverallRating();

                    log.info(stock.getName() + " (" + stock.getStockType() + ")" + " - old rating: " + oldRating + " new rating: " + newRating);

                    StockType stockType = stock.getStockType();

                    if (stockType == StockType.SMALL_CAP || stockType == StockType.MID_CAP
                            || stockType == StockType.SMALL_FINANCE || stockType == StockType.MID_FINANCE) {
                        if (oldRating < 7 && newRating >= 7) {
                            sendNewRatingMail(stock, newRating, oldRating);
                        }
                        if (oldRating > 4 && newRating <= 4) {
                            sendNewRatingMail(stock, newRating, oldRating);
                        }
                    } else {
                        if (oldRating < 4 && newRating >= 4) {
                            sendNewRatingMail(stock, newRating, oldRating);
                        }
                        if (oldRating > 2 && newRating <= 2) {
                            sendNewRatingMail(stock, newRating, oldRating);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                    log.info("Disabling automatic rating for " + stock.getSymbol());
                    stock.setAutomaticRating(false);
                    fundamentalDataRepository.save(stock);
                    sendRatingsDisabledMail(stock);
                }
            }
        }
    }

    private void sendNewRatingMail(FundamentalData stock, int newRating, int oldRating) {
        log.info("Sending email...");
        emailService.send("Neues Rating: " + stock.getName() + " (" + stock.getSymbol() +")",
                "Für " + stock.getName() + " gibt es ein neues Rating: " + newRating + " (" + oldRating + ")");
    }

    private void sendRatingsDisabledMail(FundamentalData stock) {
        emailService.send("Automatische Ratings deaktiviert: " + stock.getName() + " (" + stock.getSymbol() +")",
                "Für " + stock.getName() + " wurden aufgrund eines Fehlers die automatischen Ratings deaktiviert");
    }
}
