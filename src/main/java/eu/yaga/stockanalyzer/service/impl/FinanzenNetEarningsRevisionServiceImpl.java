package eu.yaga.stockanalyzer.service.impl;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.model.FundamentalDataUrl;
import eu.yaga.stockanalyzer.model.FundamentalDataUrlType;
import eu.yaga.stockanalyzer.parser.FinanzenNetParser;
import eu.yaga.stockanalyzer.service.EarningsRevisionService;
import eu.yaga.stockanalyzer.util.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implementation of {@link EarningsRevisionService} parsing data from finanzen.net
 */
public class FinanzenNetEarningsRevisionServiceImpl implements EarningsRevisionService {

    private static final Logger log = LoggerFactory.getLogger(FinanzenNetEarningsRevisionServiceImpl.class);

    @Override
    public FundamentalData retrieveEarningsRevision(FundamentalData fundamentalData) {
        URL url = null;
        try {
            for (FundamentalDataUrl fdUrl : fundamentalData.getUrls()) {
                if (fdUrl.getType() == FundamentalDataUrlType.EARNINGS_REVISION) {
                    url = new URL(fdUrl.getUrl());
                    break;
                }
            }
        } catch (MalformedURLException e) {
            log.error("Error parsing url: " + e.getLocalizedMessage());
        }

        if (url != null) {
            String html = HttpHelper.queryHTML(url);
            fundamentalData = FinanzenNetParser.parseEarningsRevisions(html, fundamentalData);
        }

        return fundamentalData;
    }

}
