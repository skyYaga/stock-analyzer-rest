package eu.yaga.stockanalyzer.schedule;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.repository.FundamentalDataRepository;
import eu.yaga.stockanalyzer.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Watches the dates of quarterly figures
 */
@Component
class QuarterlyFiguresChecker {

    private static final Logger log = LoggerFactory.getLogger(QuarterlyFiguresChecker.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private FundamentalDataRepository fundamentalDataRepository;

    @Scheduled(cron = "0 0 6 * * *")
    void checkDates() {
        log.info("Checking Quarterly Figures...");

        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date oneWeekAgo = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        cal.add(Calendar.MONTH, -3);
        Date threeMonthAgo = cal.getTime();

        List<FundamentalData> stocks = fundamentalDataRepository.findAll();

        for (FundamentalData stock : stocks) {
            log.info("Checking... " + stock.getName());
            Date nextFigures = stock.getNextQuarterlyFigures();

            if (nextFigures != null && nextFigures.before(today)) {
                // Update date of last quarterly figures
                stock.setLastQuarterlyFigures(nextFigures);
                stock.setNextQuarterlyFigures(null);
                fundamentalDataRepository.save(stock);

                emailService.send("Neue Quartalszahlen: " + stock.getName() + " (" + stock.getSymbol() +")",
                        "Für " + stock.getName() + " wurden am " + stock.getLastQuarterlyFigures() +
                        " neue Quartalszahlen veröffentlicht! \n" + stock.getUrls());

                log.info("New quarterly figures released!");
            } else if (nextFigures == null && stock.getDate().before(oneWeekAgo)) {
                if (stock.getLastQuarterlyFigures().before(threeMonthAgo)) {
                    fundamentalDataRepository.save(stock);
                    emailService.send("Quartalszahlen Datum prüfen: " + stock.getName() + " (" + stock.getSymbol() +")",
                        "Für " + stock.getName() + " wurden die letzten Quartalszahlen vor mehr als 3 Montaten veröffentlicht (" +
                        stock.getLastQuarterlyFigures() + ") \n" + stock.getUrls());
                    log.info("Quarterly figures are out of date!");
                }
            }
        }
    }
}
