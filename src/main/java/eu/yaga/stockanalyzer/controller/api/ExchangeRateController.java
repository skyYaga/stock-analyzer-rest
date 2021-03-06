package eu.yaga.stockanalyzer.controller.api;

import eu.yaga.stockanalyzer.model.historicaldata.HistoricalDataQuote;
import eu.yaga.stockanalyzer.service.HistoricalExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

/**
 * REST Controller for exchange rates
 */
@RestController
@RequestMapping("/api/exchange-rate")
class ExchangeRateController {

    @Autowired
    private HistoricalExchangeRateService historicalExchangeRateService;

    /**
     * This Controller returns the historical exchange rates for the given symbol<br/>
     * The date parameters are optional
     *
     * @param symbol the stocks symbol
     * @param dateStringFrom Date of the start of the historical data (yyyy-MM-dd)
     * @param dateStringTo Date of the end of the historical data (yyyy-MM-dd)
     * @return a list of historical exchange rates
     * @throws ParseException if a submitted date is invalid
     */
    @RequestMapping(value = "/{symbol:.+}", method = RequestMethod.GET)
    public List<HistoricalDataQuote> getExchangeRateForRange(
            @PathVariable String symbol,
            @RequestParam(value = "from", required = false) String dateStringFrom,
            @RequestParam(value = "to", required = false) String dateStringTo
    ) throws ParseException {
        return historicalExchangeRateService.getHistoricalExchangeRates(symbol, dateStringFrom, dateStringTo);
    }
}
