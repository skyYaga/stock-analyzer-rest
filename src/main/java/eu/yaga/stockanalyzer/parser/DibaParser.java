package eu.yaga.stockanalyzer.parser;

import eu.yaga.stockanalyzer.model.FundamentalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses data from Diba
 */
public class DibaParser {

    private static final Logger log = LoggerFactory.getLogger(DibaParser.class);

    public static FundamentalData parseAnalystEstimation(String html, FundamentalData fd) {
        Pattern buyPattern = Pattern.compile("<div class=\"sh_analysis_col sh_analysis_col_1\">((?!</div>\\d).)*</div>(\\d*)[^\\d]");
        Pattern holdPattern = Pattern.compile("<div class=\"sh_analysis_col sh_analysis_col_2\">((?!</div>\\d).)*</div>(\\d*)[^\\d]");
        Pattern sellPattern = Pattern.compile("<div class=\"sh_analysis_col sh_analysis_col_3\">((?!</div>\\d).)*</div>(\\d*)[^\\d]");
        Matcher buyMatcher = buyPattern.matcher(html);
        Matcher holdMatcher = holdPattern.matcher(html);
        Matcher sellMatcher = sellPattern.matcher(html);

        int foundCount = 0;
        double buy = 0;
        double hold = 0;
        double sell = 0;

        if (buyMatcher.find()) {
            log.info("Buy: " + buyMatcher.group(2));
            buy = Integer.parseInt(buyMatcher.group(2));
            foundCount++;
        }

        if (holdMatcher.find()) {
            log.info("Hold: " + holdMatcher.group(2));
            hold = Integer.parseInt(holdMatcher.group(2));
            foundCount++;
        }

        if (sellMatcher.find()) {
            log.info("Sell: " + sellMatcher.group(2));
            sell = Integer.parseInt(sellMatcher.group(2));
            foundCount++;
        }

        if (foundCount == 3) {
            int totalCount = (int) (buy + hold + sell);
            double dibaRating = (buy + hold * 2 + sell * 3) / totalCount;

            fd.setAnalystEstimationCount(totalCount);
            fd.setAnalystEstimation(dibaRating);
        }

        return fd;
    }

}
