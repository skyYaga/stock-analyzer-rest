package eu.yaga.stockanalyzer.service;

import eu.yaga.stockanalyzer.model.FundamentalData;
import org.springframework.stereotype.Service;

/**
 * Service for automatic parsing of earnings revisions
 */
@Service
public interface EarningsRevisionService {

    FundamentalData retrieveEarningsRevision(FundamentalData fundamentalData);

}
