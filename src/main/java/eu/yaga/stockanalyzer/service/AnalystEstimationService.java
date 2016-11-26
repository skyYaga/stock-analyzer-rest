package eu.yaga.stockanalyzer.service;

import eu.yaga.stockanalyzer.model.FundamentalData;
import org.springframework.stereotype.Service;

/**
 * Service for analyst estimations
 */
@Service
public interface AnalystEstimationService {

    FundamentalData retrieveAnalystEstimation(FundamentalData fundamentalData);

}
