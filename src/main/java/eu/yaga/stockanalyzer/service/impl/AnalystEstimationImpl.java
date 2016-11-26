package eu.yaga.stockanalyzer.service.impl;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.model.FundamentalDataUrl;
import eu.yaga.stockanalyzer.parser.DibaParser;
import eu.yaga.stockanalyzer.parser.YahooParser;
import eu.yaga.stockanalyzer.service.AnalystEstimationService;
import eu.yaga.stockanalyzer.util.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implementation of {@link AnalystEstimationService}
 */
public class AnalystEstimationImpl implements AnalystEstimationService{

    private static final Logger log = LoggerFactory.getLogger(AnalystEstimationImpl.class);

    @Override
    public FundamentalData retrieveAnalystEstimation(FundamentalData fundamentalData) {
        FundamentalDataUrl analystUrl = null;

        for (FundamentalDataUrl fdUrl : fundamentalData.getUrls()) {
            switch (fdUrl.getType()) {
                case DIBA_ANALYST_ESTIMATION:
                    analystUrl = fdUrl;
                    break;
                case YAHOO_ANALYST_ESTIMATION:
                    analystUrl = fdUrl;
                    break;
                default:
                    break;
            }
        }

        if (analystUrl != null) {
            try {
                URL url = new URL(analystUrl.getUrl());
                String html = HttpHelper.queryHTML(url);

                switch (analystUrl.getType()) {
                    case DIBA_ANALYST_ESTIMATION:
                        fundamentalData = DibaParser.parseAnalystEstimation(html, fundamentalData);
                        break;
                    case YAHOO_ANALYST_ESTIMATION:
                        fundamentalData = YahooParser.parseAnalystEstimation(html, fundamentalData);
                        break;
                    default:
                        break;
                }
            } catch (MalformedURLException e) {
                log.error("Error parsing url: " + e.getLocalizedMessage());
            }
        }

        return fundamentalData;
    }
}
