package eu.yaga.stockanalyzer.service.impl;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.model.FundamentalDataUrl;
import eu.yaga.stockanalyzer.model.FundamentalDataUrlType;
import eu.yaga.stockanalyzer.parser.OnVistaParser;
import eu.yaga.stockanalyzer.service.FundamentalDataService;
import eu.yaga.stockanalyzer.util.HttpHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Implementation of the {@link FundamentalDataService} getting data from onvista
 */
public class OnVistaFundamentalDataServiceImpl implements FundamentalDataService {

    private static final Logger log = LoggerFactory.getLogger(OnVistaFundamentalDataServiceImpl.class);

    @Autowired
    private
    OnVistaParser onVistaParser;

    /**
     * This method returns fundamental data of the given stock
     *
     * @param symbol Symbol of the stock
     * @return fundamental data
     */
    @Override
    public FundamentalData getFundamentalData(String symbol, FundamentalData fundamentalData) {
        List<FundamentalDataUrl> urls = fundamentalData.getUrls();
        URL url = null;
        for (FundamentalDataUrl fdUrl : urls) {
            if (fdUrl.getType() == FundamentalDataUrlType.ONVISTA_FUNDAMENTAL_DATA) {
                try {
                    url = new URL(fdUrl.getUrl());
                } catch (MalformedURLException e) {
                    log.error("Unable to parse URL: " + fdUrl.getUrl(), e.getLocalizedMessage());
                }
            }
        }

        if (url == null) {
            url = getUrlForSymbol(symbol);
        }

        String html = HttpHelper.queryHTML(url);
        return onVistaParser.getFundamentalData(html, fundamentalData);
    }

    private URL getUrlForSymbol(String symbol) {
        URL resultUrl = null;

        String searchSymbol = symbol.split("\\.")[0];

        try {
            URL searchUrl =
                    new URL("http://www.onvista.de/onvista/boxes/assetSearch.json?doSubmit=Suchen&portfolioName=&searchValue=" + searchSymbol);

            String html = HttpHelper.queryHTML(searchUrl);
            JSONObject jsonObject = new JSONObject(html);
            JSONArray jsonArray = jsonObject.getJSONObject("onvista").getJSONObject("results").getJSONArray("asset");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = (JSONObject) jsonArray.get(i);
                if (obj.getString("type").equals("Aktie")) {
                    String urlString = obj.getString("snapshotlink");
                    resultUrl = new URL(urlString.replace("/aktien/", "/aktien/fundamental/"));
                    break;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return resultUrl;
    }

}
