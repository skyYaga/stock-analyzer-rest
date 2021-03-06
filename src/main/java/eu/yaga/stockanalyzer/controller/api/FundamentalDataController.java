package eu.yaga.stockanalyzer.controller.api;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.repository.FundamentalDataRepository;
import eu.yaga.stockanalyzer.service.AnalystEstimationService;
import eu.yaga.stockanalyzer.service.EarningsRevisionService;
import eu.yaga.stockanalyzer.service.FundamentalDataService;
import eu.yaga.stockanalyzer.service.StockRatingBusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * REST Controller for fundamental data
 */
@RestController
@RequestMapping("/api/fundamental-data")
class FundamentalDataController {

    private static final Logger log = LoggerFactory.getLogger(FundamentalDataController.class);

    @Autowired
    private FundamentalDataService fundamentalDataService;

    @Autowired
    private EarningsRevisionService earningsRevisionService;

    @Autowired
    private AnalystEstimationService analystEstimationService;

    @Autowired
    private StockRatingBusinessService stockRatingBusinessService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private FundamentalDataRepository fundamentalDataRepository;


    /**
     * This Controller returns all symbols with cached fundamental data<br/>
     *
     * @return stock symbols
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public LinkedHashSet<FundamentalData> getCachedSymbols() throws ParseException {
        Query query = new Query();
        query.fields().include("symbol");
        query.with(new Sort(new Sort.Order(Sort.Direction.ASC, "symbol")));
        List<FundamentalData> fundamentalDataList = mongoTemplate.find(query, FundamentalData.class);
        LinkedHashSet<String> symbols = new LinkedHashSet<>();
        for (FundamentalData data : fundamentalDataList) {
            symbols.add(data.getSymbol());
        }

        LinkedHashSet<FundamentalData> sortedFundamentalDataList = new LinkedHashSet<>();
        for (String symbol : symbols) {
            sortedFundamentalDataList.add(fundamentalDataRepository.findBySymbolOrderByDateDesc(symbol));
        }

        return sortedFundamentalDataList;
    }

    /**
     * This Controller saves fundamental data and returns it<br/>
     *
     * @param fundamentalData fundamental data
     * @return the fundamental data
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public FundamentalData saveFundamentalData(@RequestBody FundamentalData fundamentalData) {
        return fundamentalDataRepository.save(fundamentalData);
    }

    /**
     * This Controller returns the fundamental data for the given symbol<br/>
     *
     * @param symbol the stocks symbol
     * @return the fundamental data
     */
    @RequestMapping(value = "/{symbol:.+}", method = RequestMethod.GET)
    public FundamentalData getFundamentalData(@PathVariable String symbol) throws ParseException {
        return fundamentalDataRepository.findBySymbolOrderByDateDesc(symbol);
    }

    /**
     * This Controller refreshes the fundamental data for the given symbol<br/>
     *
     * @param symbol the stocks symbol
     * @return the fundamental data
     */
    @RequestMapping(value = "/{symbol}/refresh", method = RequestMethod.GET)
    public FundamentalData refreshFundamentalData(@PathVariable String symbol) throws ParseException {
        FundamentalData fundamentalData = fundamentalDataRepository.findBySymbolOrderByDateDesc(symbol);
        log.info("Got Fundamental Data: " + fundamentalData);

        FundamentalData newFundamentalData = fundamentalDataService.getFundamentalData(symbol, fundamentalData);
        log.info("Got new Fundamental Data: " + newFundamentalData);
        newFundamentalData = earningsRevisionService.retrieveEarningsRevision(newFundamentalData);
        log.info("Got Earnings Revision: " + newFundamentalData.getEarningsRevision());
        newFundamentalData = analystEstimationService.retrieveAnalystEstimation(newFundamentalData);
        log.info("Got Analyst Estimation: " + newFundamentalData.getAnalystEstimation());

        if (fundamentalData != null) {
            fundamentalData.setSymbol(newFundamentalData.getSymbol());
            fundamentalData.setDate(newFundamentalData.getDate());
            fundamentalData.setBusinessYears(newFundamentalData.getBusinessYears());
            fundamentalData.setRoe(newFundamentalData.getRoe());
            fundamentalData.setEbit(newFundamentalData.getEbit());
            fundamentalData.setEquityRatio(newFundamentalData.getEquityRatio());
            fundamentalData.setAsk(newFundamentalData.getAsk());
            fundamentalData.setEpsCurrentYear(newFundamentalData.getEpsCurrentYear());
            fundamentalData.setEpsNextYear(newFundamentalData.getEpsNextYear());
            fundamentalData.setEpsHistory(newFundamentalData.getEpsHistory());
            fundamentalData.setPer5years(newFundamentalData.getPer5years());
            fundamentalData.setPerCurrent(newFundamentalData.getPerCurrent());
            fundamentalData.setMarketCapitalization(newFundamentalData.getMarketCapitalization());
            fundamentalData.setEarningsRevision(newFundamentalData.getEarningsRevision());
            fundamentalData.setAnalystEstimation(newFundamentalData.getAnalystEstimation());
            fundamentalData.setAnalystEstimationCount(newFundamentalData.getAnalystEstimationCount());
        } else {
            fundamentalData = newFundamentalData;
        }

        fundamentalData = stockRatingBusinessService.rate(fundamentalData);
        log.info("Fundamental Data rated: " + fundamentalData);

        FundamentalData saved = fundamentalDataRepository.save(fundamentalData);
        log.info("SAVED: " + saved);

        return fundamentalData;
    }

    /**
     * Delete a symbol
     * @param symbol the stocks symbol
     */
    @RequestMapping(value = "/{symbol}/delete", method = RequestMethod.DELETE)
    public void deleteFundamentalData(@PathVariable String symbol) {
        log.info("Deleting " + symbol);
        Long deleteCount = fundamentalDataRepository.deleteBySymbol(symbol);
        log.info("Deleted " + deleteCount + " entries.");
    }

    /**
     * This Controller enables automatic ratings for fundamental data<br/>
     *
     * @param symbol the stocks symbol
     * @return the fundamental data
     */
    @RequestMapping(value = "/{symbol}/enableratings", method = RequestMethod.GET)
    public FundamentalData enableAutomaticRatings(@PathVariable String symbol) {
        FundamentalData fundamentalData = fundamentalDataRepository.findBySymbolOrderByDateDesc(symbol);
        log.info("Got Fundamental Data: " + fundamentalData);

        fundamentalData.setAutomaticRating(true);

        return fundamentalDataRepository.save(fundamentalData);
    }

    /**
     * This Controller enables all automatic ratings for fundamental data<br/>
     *
     * @return the fundamental data
     */
    @RequestMapping(value = "/enableratings", method = RequestMethod.PUT)
    public void enableAllAutomaticRatings() {
        List<FundamentalData> fundamentalDataList = fundamentalDataRepository.findAll();

        for (FundamentalData fundamentalData : fundamentalDataList) {
            if (!fundamentalData.isAutomaticRating()) {
                log.info("Enabling Fundamental Data ratings for: " + fundamentalData);
                fundamentalData.setAutomaticRating(true);
                fundamentalDataRepository.save(fundamentalData);
            }
        }
    }
}
