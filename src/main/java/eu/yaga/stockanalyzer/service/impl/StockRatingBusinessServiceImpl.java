package eu.yaga.stockanalyzer.service.impl;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.model.StockType;
import eu.yaga.stockanalyzer.service.HistoricalExchangeRateService;
import eu.yaga.stockanalyzer.service.StockRatingBusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of StockRatingBusinessService
 */
public class StockRatingBusinessServiceImpl implements StockRatingBusinessService {

    private static final Logger log = LoggerFactory.getLogger(StockRatingBusinessServiceImpl.class);

    private HistoricalExchangeRateService historicalExchangeRateService;

    public StockRatingBusinessServiceImpl(HistoricalExchangeRateService historicalExchangeRateService) {
        this.historicalExchangeRateService = historicalExchangeRateService;
    }

    /**
     * rate a stock
     *
     * @param fd the stock
     * @return the FundamentalData with ratings
     */
    @Override
    public FundamentalData rate(FundamentalData fd) {
        fd = rateRoe(fd);
        fd = rateEbit(fd);
        fd = rateEquityRatio(fd);
        fd = ratePer5years(fd);
        fd = ratePerCurrent(fd);
        fd = rateAnalystEstimation(fd);
        fd = rateQuarterlyFigures(fd);
        fd = rateRateProgress6month(fd);
        fd = rateRateProgress1year(fd);
        fd = rateRateMomentum(fd);
        fd = rateReversal3Month(fd);
        fd = rateProfitGrowth(fd);
        fd = rateEarningsRevision(fd);

        fd = rateOverall(fd);

        return fd;
    }

    private FundamentalData rateEarningsRevision(FundamentalData fd) {
        log.info("Rating earnings revision...");
        double earningsRevision = fd.getEarningsRevision();

        if (earningsRevision > 5) {
            fd.setEarningsRevisionRating(1);
        } else if (earningsRevision < -5) {
            fd.setEarningsRevisionRating(-1);
        } else {
            fd.setEarningsRevisionRating(0);
        }

        return fd;
    }

    private FundamentalData rateProfitGrowth(FundamentalData fd) {
        log.info("Rating profit growth...");
        double epsCurrentYear = fd.getEpsCurrentYear();
        double epsNextYear = fd.getEpsNextYear();

        double profitGrowth = (epsNextYear - epsCurrentYear) / epsCurrentYear * 100;
        fd.setProfitGrowth(profitGrowth);

        if (profitGrowth > 5) {
            fd.setProfitGrowthRating(1);
        } else if (profitGrowth < -5) {
            fd.setProfitGrowthRating(-1);
        } else {
            fd.setProfitGrowthRating(0);
        }

        return fd;
    }

    private FundamentalData rateReversal3Month(FundamentalData fd) {
        log.info("Rating 3 month reversal...");
        StockType stockType = fd.getStockType();

        if (stockType.equals(StockType.MID_CAP) || stockType.equals(StockType.SMALL_CAP)) {
            fd.setReversal3Month(new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0)));
            fd.setReversal3MonthRating(0);
        } else {
            List<Double> reversal3Month = historicalExchangeRateService.getReversal3Month(fd);
            fd.setReversal3Month(reversal3Month);

            int score = 0;
            if (reversal3Month != null) {
                for (double reversal : reversal3Month) {
                    if (reversal > 0) {
                        score++;
                    } else if (reversal < 0) {
                        score--;
                    }
                }
            }

            switch (score) {
                case 3:
                    fd.setReversal3MonthRating(-1);
                    break;
                case -3:
                    fd.setReversal3MonthRating(1);
                    break;
                default: fd.setReversal3MonthRating(0);
            }
        }

        return fd;
    }

    private FundamentalData rateRateMomentum(FundamentalData fd) {
        log.info("Rating rate momentum...");
        int rateProgress6monthRating = fd.getRateProgress6monthRating();
        int rateProgress1yearRating = fd.getRateProgress1yearRating();

        if (rateProgress6monthRating == 1 && rateProgress1yearRating <= 0) {
            fd.setRateMomentumRating(1);
        } else if (rateProgress6monthRating == -1 && rateProgress1yearRating >= 0) {
            fd.setRateMomentumRating(-1);
        } else {
            fd.setRateMomentumRating(0);
        }

        return fd;
    }

    private FundamentalData rateRateProgress1year(FundamentalData fd) {
        log.info("Rating rate progress 1 year...");
        double rateProgress1year = historicalExchangeRateService.getRateProgress1year(fd);
        fd.setRateProgress1year(rateProgress1year);

        if (rateProgress1year > 5) {
            fd.setRateProgress1yearRating(1);
        } else if (rateProgress1year < -5) {
            fd.setRateProgress1yearRating(-1);
        } else {
            fd.setRateProgress1yearRating(0);
        }

        return fd;
    }

    private FundamentalData rateRateProgress6month(FundamentalData fd) {
        log.info("Rating rate progress 6 month...");
        double rateProgress6month = historicalExchangeRateService.getRateProgress6month(fd);
        fd.setRateProgress6month(rateProgress6month);

        if (rateProgress6month > 5) {
            fd.setRateProgress6monthRating(1);
        } else if (rateProgress6month < -5) {
            fd.setRateProgress6monthRating(-1);
        } else {
            fd.setRateProgress6monthRating(0);
        }

        return fd;
    }

    private FundamentalData rateQuarterlyFigures(FundamentalData fd) {
        log.info("Rating quarterly figures...");
        double reactionToQuarterlyFigures = historicalExchangeRateService.getReactionToQuarterlyFigures(fd);

        if (reactionToQuarterlyFigures >= 1) {
            fd.setLastQuarterlyFiguresRating(1);
        } else if (reactionToQuarterlyFigures <= -1) {
            fd.setLastQuarterlyFiguresRating(-1);
        } else {
            fd.setLastQuarterlyFiguresRating(0);
        }

        return fd;
    }

    private FundamentalData rateAnalystEstimation(FundamentalData fd) {
        log.info("Rating analyst estimation...");
        double analystEstimation = fd.getAnalystEstimation();
        StockType stockType = fd.getStockType();
        int analystEstimationCount = fd.getAnalystEstimationCount();
        if (stockType == StockType.SMALL_CAP && analystEstimationCount > 0 && analystEstimationCount < 5) {
            if (analystEstimation >= 2.5) {
                fd.setAnalystEstimationRating(-1);
            } else if (analystEstimation <= 1.5) {
                fd.setAnalystEstimationRating(1);
            } else {
                fd.setAnalystEstimationRating(0);
            }
        } else {
            if (analystEstimation >= 2.5) {
                fd.setAnalystEstimationRating(1);
            } else if (analystEstimation <= 1.5) {
                fd.setAnalystEstimationRating(-1);
            } else {
                fd.setAnalystEstimationRating(0);
            }
        }

        return fd;
    }

    private FundamentalData rateOverall(FundamentalData fd) {
        log.info("Creating overall rating...");
        int overallRating = fd.getRoeRating()
                + fd.getEbitRating()
                + fd.getEquityRatioRating()
                + fd.getPer5yearsRating()
                + fd.getPerCurrentRating()
                + fd.getAnalystEstimationRating()
                + fd.getLastQuarterlyFiguresRating()
                + fd.getRateProgress6monthRating()
                + fd.getRateProgress1yearRating()
                + fd.getRateMomentumRating()
                + fd.getReversal3MonthRating()
                + fd.getProfitGrowthRating()
                + fd.getEarningsRevisionRating();

        fd.setOverallRating(overallRating);
        return fd;
    }

    private FundamentalData ratePerCurrent(FundamentalData fd) {
        log.info("Rating current per...");
        double perCurrent = fd.getPerCurrent();

        if (perCurrent < 12) {
            fd.setPerCurrentRating(1);
        } else if (perCurrent > 16) {
            fd.setPerCurrentRating(-1);
        } else {
            fd.setPerCurrentRating(0);
        }

        return fd;
    }

    private FundamentalData ratePer5years(FundamentalData fd) {
        log.info("Rating per 5 years...");
        double per5years = fd.getPer5years();

        if (per5years < 12) {
            fd.setPer5yearsRating(1);
        } else if (per5years > 16) {
            fd.setPer5yearsRating(-1);
        } else {
            fd.setPer5yearsRating(0);
        }

        return fd;
    }

    private FundamentalData rateEquityRatio(FundamentalData fd) {
        log.info("Rating equity ratio...");
        double equityRatio = fd.getEquityRatio();

        if (fd.getStockType() == StockType.LARGE_FINANCE) {
            if (equityRatio > 10) {
                fd.setEquityRatioRating(1);
            } else if (equityRatio < 5) {
                fd.setEquityRatioRating(-1);
            } else {
                fd.setEquityRatioRating(0);
            }
        } else {
            if (equityRatio > 25) {
                fd.setEquityRatioRating(1);
            } else if (equityRatio < 15) {
                fd.setEquityRatioRating(-1);
            } else {
                fd.setEquityRatioRating(0);
            }
        }

        return fd;
    }

    private FundamentalData rateEbit(FundamentalData fd) {
        log.info("Rating ebit...");
        if (fd.getStockType() == StockType.LARGE_FINANCE) {
            fd.setEbitRating(0);
        } else {
            double ebit = fd.getEbit();

            if (ebit > 12) {
                fd.setEbitRating(1);
            } else if (ebit < 6) {
                fd.setEbitRating(-1);
            } else {
                fd.setEbitRating(0);
            }
        }

        return fd;
    }

    private FundamentalData rateRoe(FundamentalData fd) {
        log.info("Rating roe...");
        double roe = fd.getRoe();

        if (roe > 20) {
            fd.setRoeRating(1);
        } else if (roe < 10) {
            fd.setRoeRating(-1);
        } else {
            fd.setRoeRating(0);
        }

        return fd;
    }
}
