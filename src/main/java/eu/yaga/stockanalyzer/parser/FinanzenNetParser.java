package eu.yaga.stockanalyzer.parser;

import eu.yaga.stockanalyzer.model.FundamentalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses data from finanzen.net
 */
public class FinanzenNetParser {

    private static final Logger log = LoggerFactory.getLogger(FinanzenNetParser.class);

    public static FundamentalData parseEarningsRevisions(String html, FundamentalData fd) {
        double earningsRevision = 0;
        Pattern positivPattern = Pattern.compile("<td>Positive Analystenhaltung");
        Pattern negativPattern = Pattern.compile("<td>Negative Analystenhaltung");
        Matcher posMatcher = positivPattern.matcher(html);
        Matcher negMatcher = negativPattern.matcher(html);

        while (posMatcher.find()) {
            log.info("Found positive earningsRevisions!");
            earningsRevision = 6;
        }

        while (negMatcher.find()) {
            log.info("Found negative earningsRevisions!");
            earningsRevision = -6;
        }

        fd.setEarningsRevision(earningsRevision);

        return fd;
    }
}
