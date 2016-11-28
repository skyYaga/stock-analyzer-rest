package eu.yaga.stockanalyzer.schedule;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.repository.FundamentalDataRepository;
import eu.yaga.stockanalyzer.service.EmailService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests for {@link QuarterlyFiguresChecker}
 */
public class RatingBotTest {

    @InjectMocks
    RatingBot ratingBot;

    @Mock
    EmailService emailService;

    @Mock
    FundamentalDataRepository fundamentalDataRepository;

    @Mock
    RestTemplate restTemplate;

    @Test
    public void testRatingNeededOneWeek() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -8);
        Date old = cal.getTime();

        List<FundamentalData> fdList = createDummyData(old, old, 0);
        List<FundamentalData> fdListAfter = createDummyData(old, new Date(), 1);
        FundamentalData fdAfter = fdListAfter.get(0);

        initMocks(this);
        doReturn(fdList).when(fundamentalDataRepository).findAll();
        doReturn(fdAfter).when(restTemplate).getForObject("http://localhost:8081/api/fundamental-data/" + fdAfter.getSymbol() + "/refresh", FundamentalData.class);

        ratingBot.rateStocks();

        verify(fundamentalDataRepository, times(1)).findAll();
        verify(restTemplate, times(1)).getForObject("http://localhost:8081/api/fundamental-data/" + fdAfter.getSymbol() + "/refresh", FundamentalData.class);
        verify(emailService, times(1)).send("Neues Rating: Abcde (ABC.DE)",
                "Für Abcde gibt es ein neues Rating: 1 (0)");
    }

    @Test
    public void testRatingNeededNoMail() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -8);
        Date old = cal.getTime();

        List<FundamentalData> fdList = createDummyData(old, old, 0);
        FundamentalData fdAfter = fdList.get(0);

        initMocks(this);
        doReturn(fdList).when(fundamentalDataRepository).findAll();
        doReturn(fdAfter).when(restTemplate).getForObject("http://localhost:8081/api/fundamental-data/" + fdAfter.getSymbol() + "/refresh", FundamentalData.class);

        ratingBot.rateStocks();

        verify(fundamentalDataRepository, times(1)).findAll();
        verify(restTemplate, times(1)).getForObject("http://localhost:8081/api/fundamental-data/" + fdAfter.getSymbol() + "/refresh", FundamentalData.class);
        verify(emailService, times(0)).send(anyString(), anyString());
    }

    @Test
    public void testRatingNeededQuarterlyReleased() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2);
        Date lastQuarterly = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -3);
        Date lastRating = cal.getTime();

        List<FundamentalData> fdList = createDummyData(lastQuarterly, lastRating, 0);
        List<FundamentalData> fdListAfter = createDummyData(lastQuarterly, new Date(), 1);
        FundamentalData fdAfter = fdListAfter.get(0);

        initMocks(this);
        doReturn(fdList).when(fundamentalDataRepository).findAll();
        doReturn(fdAfter).when(restTemplate).getForObject("http://localhost:8081/api/fundamental-data/" + fdAfter.getSymbol() + "/refresh", FundamentalData.class);

        ratingBot.rateStocks();

        verify(fundamentalDataRepository, times(1)).findAll();
        verify(restTemplate, times(1)).getForObject("http://localhost:8081/api/fundamental-data/" + fdAfter.getSymbol() + "/refresh", FundamentalData.class);
        verify(emailService, times(1)).send("Neues Rating: Abcde (ABC.DE)",
                "Für Abcde gibt es ein neues Rating: 1 (0)");
    }

    @Test
    public void testNoRatingNeeded() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -4);
        Date oldRating = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -10);
        Date oldResults = cal.getTime();

        List<FundamentalData> fdList = createDummyData(oldResults, oldRating, 0);

        initMocks(this);
        doReturn(fdList).when(fundamentalDataRepository).findAll();

        ratingBot.rateStocks();

        verify(fundamentalDataRepository, times(1)).findAll();
        verify(restTemplate, times(0)).getForObject(anyString(), eq(FundamentalData.class));
        verify(emailService, times(0)).send(anyString(), anyString());
    }

    private List<FundamentalData> createDummyData(Date lastQuarterlyFigures, Date lastRating, int rating) {
        List<FundamentalData> fdList = new ArrayList<>();
        FundamentalData fd = new FundamentalData();
        fd.setSymbol("ABC.DE");
        fd.setName("Abcde");
        fd.setDate(lastRating);
        fd.setLastQuarterlyFigures(lastQuarterlyFigures);
        fd.setOverallRating(rating);
        fdList.add(fd);

        return fdList;
    }
}
