package eu.yaga.stockanalyzer.parser;

import eu.yaga.stockanalyzer.model.FundamentalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses data from Yahoo finance
 */
public class YahooParser {

    private static final Logger log = LoggerFactory.getLogger(YahooParser.class);

    public static FundamentalData parseAnalystEstimation(String html, FundamentalData fd) {
        Pattern pattern = Pattern.compile("<tr><td [^<]*Durchschn\\. Empfehlung \\(diese Woche\\):</td><td[^>]*>(\\d,\\d)</td>");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            log.info(matcher.group(0));
            log.info(matcher.group(1));
            double yahooRating = Double.parseDouble(matcher.group(1).replace(",", "."));

            double convertedRating = ((yahooRating - 1) / 4) * 2 + 1;

            fd.setAnalystEstimation(convertedRating);
        }

        return fd;
    }
}
