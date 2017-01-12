package eu.yaga.stockanalyzer.service.impl;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.model.StockType;
import eu.yaga.stockanalyzer.service.HistoricalExchangeRateService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for StockRatingBusinessServiceImpl
 */
public class StockRatingBusinessServiceImplTest {

    private StockRatingBusinessServiceImpl service;
    private FundamentalData fd;

    @Before
    public void setUp() {
        fd = new FundamentalData();
        fd.setStockType(StockType.LARGE_CAP);
    }

    @Test
    public void testGoodRating() {
        fd.setRoe(21);
        fd.setEbit(13);
        fd.setEquityRatio(26);
        fd.setEpsCurrentYear(10);
        fd.setEpsNextYear(11);
        fd.setPer5years(11);
        fd.setPerCurrent(11);
        fd.setAnalystEstimation(2.6);
        fd.setLastQuarterlyFigures(new Date());
        List<Double> reversalList = new ArrayList<>();
        reversalList.add(-1.0);
        reversalList.add(-2.0);
        reversalList.add(-3.0);
        fd.setReversal3Month(reversalList);
        fd.setEarningsRevision(6);

        HistoricalExchangeRateService mockedRateService = mock(HistoricalExchangeRateService.class);
        when(mockedRateService.getReactionToQuarterlyFigures(fd)).thenReturn(1.1);
        when(mockedRateService.getRateProgress6month(fd)).thenReturn(6.0);
        when(mockedRateService.getRateProgress1year(fd)).thenReturn(7.0);
        when(mockedRateService.getReversal3Month(fd)).thenReturn(reversalList);
        service = new StockRatingBusinessServiceImpl(mockedRateService);

        fd = service.rate(fd);

        Assert.assertEquals("RateProgress6month", 6.0, fd.getRateProgress6month(), 0);
        Assert.assertEquals("RateProgress6monthRating", 1, fd.getRateProgress6monthRating());
        Assert.assertEquals("RateProgress1year", 7.0, fd.getRateProgress1year(), 0);
        Assert.assertEquals("RateProgress1yearRating", 1, fd.getRateProgress1yearRating());
        Assert.assertEquals("Reversal3MonthRating", 1, fd.getReversal3MonthRating());
        Assert.assertEquals("RateMomentumRating", 0, fd.getRateMomentumRating());
        Assert.assertEquals("ProfitGrowth", 10, fd.getProfitGrowth(), 0);
        Assert.assertEquals("ProfitGrowthRating", 1, fd.getProfitGrowthRating());
        Assert.assertEquals("EarningsRevisionRating", 1, fd.getEarningsRevisionRating());
        Assert.assertEquals("OverallRating", 12, fd.getOverallRating());
    }

    @Test
    public void testNeutralRating() {
        fd.setRoe(15);
        fd.setEbit(9);
        fd.setEquityRatio(20);
        fd.setEpsCurrentYear(10);
        fd.setEpsNextYear(10.1);
        fd.setPer5years(14);
        fd.setPerCurrent(14);
        fd.setAnalystEstimation(2);
        fd.setLastQuarterlyFigures(new Date());
        List<Double> reversalList = new ArrayList<>();
        reversalList.add(-1.0);
        reversalList.add(-2.0);
        reversalList.add(3.0);
        fd.setReversal3Month(reversalList);
        fd.setEarningsRevision(0);

        HistoricalExchangeRateService mockedRateService = mock(HistoricalExchangeRateService.class);
        when(mockedRateService.getReactionToQuarterlyFigures(fd)).thenReturn(0.0);
        when(mockedRateService.getRateProgress6month(fd)).thenReturn(2.0);
        when(mockedRateService.getRateProgress1year(fd)).thenReturn(-2.0);
        when(mockedRateService.getReversal3Month(fd)).thenReturn(reversalList);
        service = new StockRatingBusinessServiceImpl(mockedRateService);

        fd = service.rate(fd);

        Assert.assertEquals("roeRating", 0, fd.getRoeRating());
        Assert.assertEquals("ebitRating", 0, fd.getEbitRating());
        Assert.assertEquals("equityRatioRating", 0, fd.getEquityRatioRating());
        Assert.assertEquals("Per5yearsRating", 0, fd.getPer5yearsRating());
        Assert.assertEquals("PerCurrentRating", 0, fd.getPerCurrentRating());
        Assert.assertEquals("AnalystEstimationRating", 0, fd.getAnalystEstimationRating());
        Assert.assertEquals("LastQuarterlyFiguresRating", 0, fd.getLastQuarterlyFiguresRating());
        Assert.assertEquals("RateProgress6month", 2.0, fd.getRateProgress6month(), 0);
        Assert.assertEquals("RateProgress6monthRating", 0, fd.getRateProgress6monthRating());
        Assert.assertEquals("RateProgress1year", -2.0, fd.getRateProgress1year(), 0);
        Assert.assertEquals("RateProgress1yearRating", 0, fd.getRateProgress1yearRating());
        Assert.assertEquals("RateMomentumRating", 0, fd.getRateMomentumRating());
        Assert.assertEquals("Reversal3MonthRating", 0, fd.getReversal3MonthRating());
        Assert.assertEquals("ProfitGrowth", 1.0, fd.getProfitGrowth(), 0.00001);
        Assert.assertEquals("ProfitGrowthRating", 0, fd.getProfitGrowthRating());
        Assert.assertEquals("EarningsRevisionRating", 0, fd.getEarningsRevisionRating());
        Assert.assertEquals("OverallRating", 0, fd.getOverallRating());
    }

    @Test
    public void testBadRating() {
        fd.setRoe(9);
        fd.setEbit(5);
        fd.setEquityRatio(14);
        fd.setEpsCurrentYear(10);
        fd.setEpsNextYear(9);
        fd.setPer5years(17);
        fd.setPerCurrent(17);
        fd.setAnalystEstimation(1.5);
        fd.setLastQuarterlyFigures(new Date());
        List<Double> reversalList = new ArrayList<>();
        reversalList.add(1.0);
        reversalList.add(2.0);
        reversalList.add(3.0);
        fd.setReversal3Month(reversalList);
        fd.setEarningsRevision(-6);

        HistoricalExchangeRateService mockedRateService = mock(HistoricalExchangeRateService.class);
        when(mockedRateService.getReactionToQuarterlyFigures(fd)).thenReturn(-1.1);
        when(mockedRateService.getRateProgress6month(fd)).thenReturn(-6.0);
        when(mockedRateService.getRateProgress1year(fd)).thenReturn(-7.0);
        when(mockedRateService.getReversal3Month(fd)).thenReturn(reversalList);
        service = new StockRatingBusinessServiceImpl(mockedRateService);

        fd = service.rate(fd);

        Assert.assertEquals("RateProgress6month", -6.0, fd.getRateProgress6month(), 0);
        Assert.assertEquals("RateProgress6monthRating", -1, fd.getRateProgress6monthRating());
        Assert.assertEquals("RateProgress1year", -7.0, fd.getRateProgress1year(), 0);
        Assert.assertEquals("RateProgress1yearRating", -1, fd.getRateProgress1yearRating());
        Assert.assertEquals("RateMomentumRating", 0, fd.getRateMomentumRating());
        Assert.assertEquals("Reversal3MonthRating", -1, fd.getReversal3MonthRating());
        Assert.assertEquals("ProfitGrowth", -10, fd.getProfitGrowth(), 0);
        Assert.assertEquals("ProfitGrowthRating", -1, fd.getProfitGrowthRating());
        Assert.assertEquals("EarningsRevisionRating", -1, fd.getEarningsRevisionRating());
        Assert.assertEquals("OverallRating", -12, fd.getOverallRating());
    }

    @Test
    public void testGoodRateMomentumRating() {
        HistoricalExchangeRateService mockedRateService = mock(HistoricalExchangeRateService.class);
        when(mockedRateService.getRateProgress6month(fd)).thenReturn(6.0);
        when(mockedRateService.getRateProgress1year(fd)).thenReturn(0.0);
        service = new StockRatingBusinessServiceImpl(mockedRateService);

        fd = service.rate(fd);

        Assert.assertEquals("RateMomentumRating", 1, fd.getRateMomentumRating());
    }

    @Test
    public void testBadRateMomentumRating() {
        HistoricalExchangeRateService mockedRateService = mock(HistoricalExchangeRateService.class);
        when(mockedRateService.getRateProgress6month(fd)).thenReturn(-6.0);
        when(mockedRateService.getRateProgress1year(fd)).thenReturn(0.0);
        service = new StockRatingBusinessServiceImpl(mockedRateService);

        fd = service.rate(fd);

        Assert.assertEquals("RateMomentumRating", -1, fd.getRateMomentumRating());
    }

    @Test
    public void testSmallCapAnalystRating() {
        HistoricalExchangeRateService mockedRateService = mock(HistoricalExchangeRateService.class);
        service = new StockRatingBusinessServiceImpl(mockedRateService);

        fd.setStockType(StockType.SMALL_CAP);
        fd.setAnalystEstimation(1.1);
        fd = service.rate(fd);
        Assert.assertEquals("AnalystRating small cap few estimations", -1, fd.getAnalystEstimationRating());


        fd.setAnalystEstimationCount(3);
        fd = service.rate(fd);
        Assert.assertEquals("AnalystRating small cap few estimations", 1, fd.getAnalystEstimationRating());

        fd.setAnalystEstimationCount(6);
        fd = service.rate(fd);
        Assert.assertEquals("AnalystRating small cap many estimations", -1, fd.getAnalystEstimationRating());
    }

    @Test
    public void testNegativePer() {
        HistoricalExchangeRateService mockedRateService = mock(HistoricalExchangeRateService.class);
        service = new StockRatingBusinessServiceImpl(mockedRateService);

        fd.setPerCurrent(-1);
        fd.setPer5years(0);

        fd = service.rate(fd);

        Assert.assertEquals("PER current rating", -1, fd.getPerCurrentRating());
        Assert.assertEquals("PER 5 years rating", -1, fd.getPer5yearsRating());
    }
}
