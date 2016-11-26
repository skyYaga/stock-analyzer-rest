package eu.yaga.stockanalyzer.schedule;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.model.FundamentalDataUrl;
import eu.yaga.stockanalyzer.repository.FundamentalDataRepository;
import eu.yaga.stockanalyzer.service.EmailService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests for {@link QuarterlyFiguresChecker}
 */
public class QuarterlyFiguresCheckerTest {

    @InjectMocks
    QuarterlyFiguresChecker quarterlyFiguresChecker;

    @Mock
    EmailService emailService;

    @Mock
    FundamentalDataRepository fundamentalDataRepository;


    @Test
    public void testCheckNeeded() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();
        cal.add(Calendar.MONTH, -3);
        Date threeMonthAgo = cal.getTime();

        List<FundamentalData> fdList = createDummyData(threeMonthAgo, yesterday);

        initMocks(this);
        doReturn(fdList).when(fundamentalDataRepository).findAll();

        quarterlyFiguresChecker.checkDates();

        verify(fundamentalDataRepository, times(1)).findAll();
        verify(fundamentalDataRepository, times(1)).save(any(FundamentalData.class));
        verify(emailService, times(1)).send("Neue Quartalszahlen: Abcde (ABC.DE)",
                "Für Abcde wurden am " + yesterday + " neue Quartalszahlen veröffentlicht! \nhttp://foo\nhttps://bar\n");
    }

    @Test
    public void testNoCheckNeeded() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 10);
        Date yesterday = cal.getTime();
        cal.add(Calendar.MONTH, -2);
        Date threeMonthAgo = cal.getTime();

        List<FundamentalData> fdList = createDummyData(threeMonthAgo, yesterday);

        initMocks(this);
        doReturn(fdList).when(fundamentalDataRepository).findAll();

        quarterlyFiguresChecker.checkDates();

        verify(fundamentalDataRepository, times(1)).findAll();
        verify(fundamentalDataRepository, never()).save(any(FundamentalData.class));
        verify(emailService, never()).send(anyString(), anyString());
    }

    @Test
    public void testOutOfDate() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.MONTH, -4);
        Date fourMonthAgo = cal.getTime();

        List<FundamentalData> fdList = createDummyData(fourMonthAgo, null);

        initMocks(this);
        doReturn(fdList).when(fundamentalDataRepository).findAll();

        quarterlyFiguresChecker.checkDates();

        verify(fundamentalDataRepository, times(1)).findAll();
        verify(fundamentalDataRepository, times(1)).save(any(FundamentalData.class));
        verify(emailService, times(1)).send("Quartalszahlen Datum prüfen: Abcde (ABC.DE)",
                "Für Abcde wurden die letzten Quartalszahlen vor mehr als 3 Montaten veröffentlicht (" +
                 fourMonthAgo + ") \nhttp://foo\nhttps://bar\n");
    }

    private List<FundamentalData> createDummyData(Date lastQuarterlyFigures, Date nextQuarterlyFigures) {
        List<FundamentalDataUrl> urls = new ArrayList<>(Arrays.asList(
                new FundamentalDataUrl("http://foo"),
                new FundamentalDataUrl("https://bar")));

        List<FundamentalData> fdList = new ArrayList<>();
        FundamentalData fd = new FundamentalData();
        fd.setSymbol("ABC.DE");
        fd.setName("Abcde");
        fd.setDate(lastQuarterlyFigures);
        fd.setLastQuarterlyFigures(lastQuarterlyFigures);
        fd.setNextQuarterlyFigures(nextQuarterlyFigures);
        fd.setUrls(urls);
        fdList.add(fd);

        return fdList;
    }
}
