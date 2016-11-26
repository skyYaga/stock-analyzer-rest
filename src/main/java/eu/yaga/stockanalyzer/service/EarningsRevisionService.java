package eu.yaga.stockanalyzer.service;

import eu.yaga.stockanalyzer.model.FundamentalData;

/**
 * Service for automatic parsing of earnings revisions
 */
public interface EarningsRevisionService {

    FundamentalData retrieveEarningsRevision(FundamentalData fundamentalData);

}
