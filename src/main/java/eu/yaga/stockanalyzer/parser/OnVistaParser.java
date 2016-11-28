package eu.yaga.stockanalyzer.parser;

import eu.yaga.stockanalyzer.model.FundamentalData;
import eu.yaga.stockanalyzer.model.StockType;
import eu.yaga.stockanalyzer.model.historicaldata.HistoricalDataQuote;
import eu.yaga.stockanalyzer.service.CurrentStockQuotesService;
import eu.yaga.stockanalyzer.service.HistoricalExchangeRateService;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses data from onvista.de
 */
@Service
public class OnVistaParser {

    @Autowired
    private CurrentStockQuotesService currentStockQuotesService;

    @Autowired
    private HistoricalExchangeRateService historicalExchangeRateService;

    private static final Logger log = LoggerFactory.getLogger(OnVistaParser.class);

    private String html;
    private FundamentalData fundamentalData;
    private Matcher matcher;

    public OnVistaParser() {}

    public FundamentalData getFundamentalData(String html, FundamentalData fd) {
        this.html = html;
        String symbol = fd.getSymbol();
        fundamentalData = fd;

        fundamentalData.setSymbol(symbol);
        fundamentalData.setDate(new Date());

        String fiscalYearEnd = parseFiscalYearEnd();
        ArrayList<String> years = parseBusinessYears(fiscalYearEnd);
        ArrayList<String> earningYears = parseEarningYears();
        ArrayList<String> profitabilityYears = parseProfitabilityYears();
        ArrayList<String> balanceSheetYears = parseBalanceSheetYears();

        fundamentalData.setBusinessYears(years);

        // Eigenkapitalrendite / ROE
        fundamentalData.setRoe(parseRoe(profitabilityYears));

        // EBIT-Marge
        fundamentalData.setEbit(parseEbit(profitabilityYears));

        // Marktkapitalisierung
        fundamentalData.setMarketCapitalization(parseMarketCapitalization(profitabilityYears));

        // Eigenkapitalquote
        fundamentalData.setEquityRatio(parseEquityRatio(balanceSheetYears));

        // KGV
        Map<String, String> earningsPerShare = parseEarningsPerShare(earningYears);
        double currentRate = currentStockQuotesService.getCurrentRate(symbol);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();
        while (currentRate == 0) {
            try {
                List<HistoricalDataQuote> historicalExchangeRates = historicalExchangeRateService.getHistoricalExchangeRates(symbol, now.minusDays(1).format(dtf), now.format(dtf));
                if (historicalExchangeRates.size() > 0) {
                    currentRate = historicalExchangeRates.get(0).getClose();
                }
                now = now.minusDays(1);
            } catch (ParseException e) {
                log.error(e.getLocalizedMessage());
            }
        }
        fundamentalData.setAsk(currentRate);

        // Gewinn pro Aktie
        fundamentalData.setEpsNextYear(parseToDoubleOrZero(earningsPerShare.get(fundamentalData.getNextYear()).replace(",", ".")));
        fundamentalData.setEpsCurrentYear(parseToDoubleOrZero(earningsPerShare.get(fundamentalData.getCurrentYear()).replace(",", ".")));

        // 5 Jahre
        fundamentalData.setPer5years(calculatePer5years(currentRate, earningsPerShare));
        //aktuell
        fundamentalData.setPerCurrent(calculatePer(currentRate, earningsPerShare));

        return fundamentalData;
    }

    private double parseToDoubleOrZero(String numberString) {
        if (NumberUtils.isCreatable(numberString)) {
            return Double.parseDouble(numberString);
        }
        return 0;
    }


    /**
     * parses the fiscal year end (Geschäftsjahresende) for the current Stock
     * @return fiscal year end
     */
    private String parseFiscalYearEnd() {
        // Geschäftsjahresende
        String geschaeftsjahresendeString = null;
        Pattern geschaeftsjahresendePattern = Pattern.compile("<span>Geschäftsjahresende:\\s*([\\.0-9]*)</span>");
        matcher = geschaeftsjahresendePattern.matcher(html);

        while (matcher.find()) {
            log.info("Matches gefunden!");
            log.info(matcher.group(1));
            geschaeftsjahresendeString = matcher.group(1).trim();
        }

        return geschaeftsjahresendeString;
    }

    /**
     * parses the earning years for the current Stock
     * @return list of years
     */
    private ArrayList<String> parseEarningYears() {
        // Gewinn Jahresangaben

        Pattern gewinnJahresPattern = Pattern.compile("<table><thead><tr><th>\\s*Gewinn\\s*((?!</tr>).)*</tr></thead><tbody>");
        matcher = gewinnJahresPattern.matcher(html);
        ArrayList<String> gewinnJahresArray = new ArrayList<>();

        while (matcher.find()) {
            log.info("Matches gefunden!");

            log.info(matcher.group(0));
            String jahresOut =  matcher.group(0);
            gewinnJahresPattern = Pattern.compile("(\\s*<th class=\"ZAHL\">(((?!</).)*)</th>\\s*)");
            matcher = gewinnJahresPattern.matcher(jahresOut);
            while (matcher.find()) {
                log.debug(matcher.group(2));
                gewinnJahresArray.add(matcher.group(2).trim());
            }
        }

        log.info(gewinnJahresArray.toString());
        return gewinnJahresArray;
    }

    /**
     * parses the balance sheet years for the current Stock
     * @return list of years
     */
    private ArrayList<String> parseBalanceSheetYears() {
        // Bilanz Jahresangaben

        Pattern bilanzJahresPattern = Pattern.compile("<table><thead><tr><th>\\s*Bilanz\\s*((?!</tr>).)*</tr></thead><tbody>");
        matcher = bilanzJahresPattern.matcher(html);
        ArrayList<String> balanceSheetArray = new ArrayList<>();

        while (matcher.find()) {
            log.info("Matches gefunden!");

            log.info(matcher.group(0));
            String bilanzJahresOut =  matcher.group(0);
            bilanzJahresPattern = Pattern.compile("(\\s*<th class=\"ZAHL\">(((?!</).)*)</th>\\s*)");
            matcher = bilanzJahresPattern.matcher(bilanzJahresOut);
            while (matcher.find()) {
                log.debug(matcher.group(2));
                balanceSheetArray.add(matcher.group(2).trim());
            }
        }

        log.info(balanceSheetArray.toString());
        return balanceSheetArray;
    }


    /**
     * get an array of the business years from next year to three years ago
     *
     * @return a list of years
     */
    private ArrayList<String> parseBusinessYears(String fiscalYearEnd) {

        Calendar todayCalendar = GregorianCalendar.getInstance();

        Calendar geschaeftsjahresendeCalendar = GregorianCalendar.getInstance();
        geschaeftsjahresendeCalendar.set(
                todayCalendar.get(Calendar.YEAR),
                Integer.parseInt(fiscalYearEnd.substring(3, 5)) - 1,
                Integer.parseInt(fiscalYearEnd.substring(0, 2)),
                23, 59, 59);
        log.info(geschaeftsjahresendeCalendar.getTime().toString());


        String nextYearString = null;
        String currentYearString = null;
        String lastYearString = null;
        String twoYearsAgoString = null;
        String threeYearsAgoString = null;

        int inTwoYears = todayCalendar.get(Calendar.YEAR) + 2;
        int nextYear = todayCalendar.get(Calendar.YEAR) + 1;
        int currentYear = todayCalendar.get(Calendar.YEAR);
        int lastYear = todayCalendar.get(Calendar.YEAR) - 1;
        int twoYearsAgo = todayCalendar.get(Calendar.YEAR) - 2;
        int threeYearsAgo = todayCalendar.get(Calendar.YEAR) - 3;
        int fourYearsAgo = todayCalendar.get(Calendar.YEAR) - 4;

        if (fiscalYearEnd.equals("31.12.")) {
            nextYearString = nextYear + "e";
            currentYearString = currentYear + "e";
            lastYearString = String.valueOf(lastYear);
            twoYearsAgoString = String.valueOf(twoYearsAgo);
            threeYearsAgoString = String.valueOf(threeYearsAgo);
        } else if (todayCalendar.before(geschaeftsjahresendeCalendar)){
            nextYearString = String.valueOf(currentYear).substring(2, 4) + "/" + String.valueOf(nextYear).substring(2, 4) + "e";
            currentYearString = String.valueOf(lastYear).substring(2, 4) + "/" + String.valueOf(currentYear).substring(2, 4) + "e";
            lastYearString = String.valueOf(twoYearsAgo).substring(2, 4) + "/" + String.valueOf(lastYear).substring(2, 4);
            twoYearsAgoString = String.valueOf(threeYearsAgo).substring(2, 4) + "/" + String.valueOf(twoYearsAgo).substring(2, 4);
            threeYearsAgoString = String.valueOf(fourYearsAgo).substring(2, 4) + "/" + String.valueOf(threeYearsAgo).substring(2, 4);
        } else if (todayCalendar.after(geschaeftsjahresendeCalendar)) {
            nextYearString = String.valueOf(nextYear).substring(2, 4) + "/" + String.valueOf(inTwoYears).substring(2, 4) + "e";
            currentYearString = String.valueOf(currentYear).substring(2, 4) + "/" + String.valueOf(nextYear).substring(2, 4) + "e";
            lastYearString = String.valueOf(lastYear).substring(2, 4) + "/" + String.valueOf(currentYear).substring(2, 4);
            twoYearsAgoString = String.valueOf(twoYearsAgo).substring(2, 4) + "/" + String.valueOf(lastYear).substring(2, 4);
            threeYearsAgoString = String.valueOf(threeYearsAgo).substring(2, 4) + "/" + String.valueOf(twoYearsAgo).substring(2, 4);
        }

        ArrayList<String> jahresArray = new ArrayList<>();
        jahresArray.addAll(Arrays.asList(nextYearString, currentYearString, lastYearString, twoYearsAgoString, threeYearsAgoString));

        log.info(jahresArray.toString());

        return jahresArray;
    }

    /**
     * parses the earnings per share (Gewinn pro Aktie)
     * @return roe
     * @param earningYears list of years
     */
    private Map<String,String> parseEarningsPerShare(ArrayList<String> earningYears) {
        // Gewinn pro Aktie (Tabelle Gewinn Jahresangaben)

        Pattern gewinnProAktiePattern = Pattern.compile("<tr>\\s*<td[^/]*Gewinn pro Aktie in EUR((?!</tr>).)*</tr>");
        matcher = gewinnProAktiePattern.matcher(html);
        ArrayList<String> gewinnProAktieArray = new ArrayList<>();

        while (matcher.find()) {
            log.info("Matches gefunden!");

            log.info(matcher.group(0));
            String gewinnProAktieOut =  matcher.group(0);
            gewinnProAktiePattern = Pattern.compile("(\\s*<td class=\"ZAHL\">(((?!</).)*)</td>\\s*)");
            matcher = gewinnProAktiePattern.matcher(gewinnProAktieOut);
            while (matcher.find()) {
                log.debug(matcher.group(2));
                gewinnProAktieArray.add(matcher.group(2).trim());
            }
        }

        log.info(gewinnProAktieArray.toString());

        if (earningYears.size() != gewinnProAktieArray.size()) {
            throw new RuntimeException("gewinnJahresArray und gewinnProAktieArray sind nicht gleich gross");
        }

        Map<String, String> gewinnMap = new HashMap<>();
        for (int i = 0; i < earningYears.size(); i++) {
            gewinnMap.put(earningYears.get(i), gewinnProAktieArray.get(i));
        }

        log.info(gewinnMap.toString());

        return gewinnMap;
    }

    /**
     * parses the profitability (Rentabilität) of the current stock
     * @return profitabilityYears
     */
    private ArrayList<String> parseProfitabilityYears() {
        // Rentabilität Jahresangaben

        Pattern rentabilitaetJahresPattern = Pattern.compile("<table><thead><tr><th>\\s*Rentabilität\\s*((?!</tr>).)*</tr></thead><tbody>");
        matcher = rentabilitaetJahresPattern.matcher(html);
        ArrayList<String> profitabilityYears = new ArrayList<>();

        while (matcher.find()) {
            log.info("Matches gefunden!");

            log.info(matcher.group(0));
            String rentabilitaetJahresOut =  matcher.group(0);
            rentabilitaetJahresPattern = Pattern.compile("(\\s*<th class=\"ZAHL\">(((?!</).)*)</th>\\s*)");
            matcher = rentabilitaetJahresPattern.matcher(rentabilitaetJahresOut);
            while (matcher.find()) {
                log.debug(matcher.group(2).trim());
                profitabilityYears.add(matcher.group(2).trim());
            }
        }

        log.info(profitabilityYears.toString());
        return profitabilityYears;
    }

    /**
     * parses the return on equity
     * @return roe
     * @param profitabilityYears the available years
     */
    private double parseRoe(ArrayList<String> profitabilityYears) {
        Pattern roePattern = Pattern.compile("<tr>\\s*<td[^/]*Eigenkapitalrendite</td>((?!</tr>).)*</tr>");
        matcher = roePattern.matcher(html);
        ArrayList<String> roeArray = new ArrayList<>();

        while (matcher.find()) {
            log.info("Matches gefunden!");

            log.info(matcher.group(0));
            String roeOut =  matcher.group(0);
            roePattern = Pattern.compile("(\\s*<td class=\"ZAHL\">(((?!</).)*)</td>\\s*)");
            matcher = roePattern.matcher(roeOut);
            while (matcher.find()) {
                log.debug(matcher.group(2));
                roeArray.add(matcher.group(2).trim());
            }
        }

        log.info(roeArray.toString());

        if (profitabilityYears.size() != roeArray.size()) {
            throw new RuntimeException("roeArray und profitabilityYears sind nicht gleich gross");
        }

        Map<String, String> roeMap = new HashMap<>();
        for (int i = 0; i < profitabilityYears.size(); i++) {
            roeMap.put(profitabilityYears.get(i), roeArray.get(i));
        }

        log.info(roeMap.get("roe last year: " + fundamentalData.getLastYear()));

        String roe = roeMap.get(fundamentalData.getLastYear());
        if (roe == null) {
            log.info("Trying " + fundamentalData.getLastYear() + "e as last year...");
            roe = roeMap.get(fundamentalData.getLastYear() + "e");
            if (roe == null) {
                log.info(roeMap.get("roe two years ago: " + fundamentalData.getLastYear()));
                roe = roeMap.get(fundamentalData.getTwoYearsAgo());
            }
        }
        return Double.parseDouble(roe.replace("%", "").replace(",", "."));
    }

    /**
     * parses the market capitalization
     * @return marketCapitalization in million euro
     * @param profitabilityYears the available years
     */
    private double parseMarketCapitalization(ArrayList<String> profitabilityYears) {
        Pattern mcPattern = Pattern.compile("<tr>\\s*<td[^/]*Marktkapitalisierung in Mio. EUR</td>((?!</tr>).)*</tr>");
        matcher = mcPattern.matcher(html);
        ArrayList<String> mcArray = new ArrayList<>();

        while (matcher.find()) {
            log.info("Matches gefunden!");

            log.info(matcher.group(0));
            String mcOut =  matcher.group(0);
            mcPattern = Pattern.compile("(\\s*<td class=\"ZAHL\">(((?!</).)*)</td>\\s*)");
            matcher = mcPattern.matcher(mcOut);
            while (matcher.find()) {
                log.debug(matcher.group(2));
                mcArray.add(matcher.group(2).trim());
            }
        }

        log.info(mcArray.toString());

        if (profitabilityYears.size() != mcArray.size()) {
            throw new RuntimeException("mcArray und profitabilityYears sind nicht gleich gross");
        }

        Map<String, String> mcMap = new HashMap<>();
        for (int i = 0; i < profitabilityYears.size(); i++) {
            mcMap.put(profitabilityYears.get(i), mcArray.get(i));
        }

        log.info(mcMap.get("mc last year: " + fundamentalData.getLastYear()));

        String mc = mcMap.get(fundamentalData.getLastYear());
        if (mc == null) {
            log.info("Trying " + fundamentalData.getLastYear() + "e as last year...");
            mc = mcMap.get(fundamentalData.getLastYear() + "e");
            if (mc == null) {
                log.info(mcMap.get("mc two years ago: " + fundamentalData.getLastYear()));
                mc = mcMap.get(fundamentalData.getTwoYearsAgo());
            }
        }

        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
        Number number;
        try {
            number = format.parse(mc);
        } catch (ParseException e) {
            log.error("Failed to parse number: " + mc);
            throw new RuntimeException("Failed to parse number: " + mc);
        }
        return number.doubleValue();
    }

    /**
     * parses the ebit
     * @return ebit
     * @param profitabilityYears the available years
     */
    private double parseEbit(ArrayList<String> profitabilityYears) {
        if (fundamentalData.getStockType() == StockType.LARGE_FINANCE) {
            return 0;
        } else {
            Pattern ebitPattern = Pattern.compile("<tr>\\s*<td[^/]*EBIT-Marge</td>((?!</tr>).)*</tr>");
            matcher = ebitPattern.matcher(html);
            ArrayList<String> ebitArray = new ArrayList<>();

            while (matcher.find()) {
                log.info("Matches gefunden!");

                log.info(matcher.group(0));
                String ebitOut = matcher.group(0);
                ebitPattern = Pattern.compile("(\\s*<td class=\"ZAHL\">(((?!</).)*)</td>\\s*)");
                matcher = ebitPattern.matcher(ebitOut);
                while (matcher.find()) {
                    log.debug(matcher.group(2));
                    ebitArray.add(matcher.group(2).trim());
                }
            }

            log.info(ebitArray.toString());

            if (profitabilityYears.size() != ebitArray.size()) {
                throw new RuntimeException("ebitArray und profitabilityYears sind nicht gleich gross");
            }

            Map<String, String> ebitMap = new HashMap<>();
            for (int i = 0; i < profitabilityYears.size(); i++) {
                ebitMap.put(profitabilityYears.get(i), ebitArray.get(i));
            }

            log.info(ebitMap.get("Ebit last year: " + fundamentalData.getLastYear()));

            String ebit = ebitMap.get(fundamentalData.getLastYear());
            if (ebit == null) {
                log.info("Trying " + fundamentalData.getLastYear() + "e as last year...");
                ebit = ebitMap.get(fundamentalData.getLastYear() + "e");
                if (ebit == null) {
                    log.info(ebitMap.get("Ebit two years ago: " + fundamentalData.getTwoYearsAgo()));
                    ebit = ebitMap.get(fundamentalData.getTwoYearsAgo());
                }
            }
            return Double.parseDouble(ebit.replace("%", "").replace(",", "."));
        }
    }


    /**
     * parses the equity ratio
     * @return equity ratio
     * @param balanceSheetYears the available years
     */
    private double parseEquityRatio(ArrayList<String> balanceSheetYears) {
        Pattern eigenkapitalquotePattern = Pattern.compile("<tr>\\s*<td[^/]*Eigenkapitalquote</td>((?!</tr>).)*</tr>");
        matcher = eigenkapitalquotePattern.matcher(html);
        ArrayList<String> equityRatioArray = new ArrayList<>();

        while (matcher.find()) {
            log.info("Matches gefunden!");

            log.info(matcher.group(0));
            String eigenkapitalquoteOut =  matcher.group(0);
            eigenkapitalquotePattern = Pattern.compile("(\\s*<td class=\"ZAHL\">(((?!</).)*)</td>\\s*)");
            matcher = eigenkapitalquotePattern.matcher(eigenkapitalquoteOut);
            while (matcher.find()) {
                log.debug(matcher.group(2));
                equityRatioArray.add(matcher.group(2).trim());
            }
        }

        log.info(equityRatioArray.toString());

        if (balanceSheetYears.size() != equityRatioArray.size()) {
            throw new RuntimeException("ebitArray und profitabilityYears sind nicht gleich gross");
        }

        Map<String, String> equityRatioMap = new HashMap<>();
        for (int i = 0; i < balanceSheetYears.size(); i++) {
            equityRatioMap.put(balanceSheetYears.get(i), equityRatioArray.get(i));
        }

        log.info("equity ratio last year: " + equityRatioMap.get(fundamentalData.getLastYear()));

        String equityRatio = equityRatioMap.get(fundamentalData.getLastYear());
        if (equityRatio == null) {
            log.info("Trying " + fundamentalData.getLastYear() + "e as last year...");
            equityRatio = equityRatioMap.get(fundamentalData.getLastYear() + "e");
            if (equityRatio == null) {
                log.info("equity ratio two years ago: " + equityRatioMap.get(fundamentalData.getLastYear()));
                equityRatio = equityRatioMap.get(fundamentalData.getTwoYearsAgo());
            }
        }
        return Double.parseDouble(equityRatio.replace("%", "").replace(",", "."));
    }

    /**
     * Calculate the current 5 yearPER (KGV)
     *
     * @param currentRate the stocks current rate
     * @param earningsPerShare eps
     * @return 5 years PER
     */
    private double calculatePer5years(double currentRate, Map<String, String> earningsPerShare) {
        double next = parseToDoubleOrZero(earningsPerShare.get(fundamentalData.getNextYear()).replace(",", "."));
        double current = parseToDoubleOrZero(earningsPerShare.get(fundamentalData.getCurrentYear()).replace(",", "."));
        // If the data is not up to date at onvista...
        String lastYearEps = earningsPerShare.get(fundamentalData.getLastYear());
        if (lastYearEps == null) {
            lastYearEps = earningsPerShare.get(fundamentalData.getLastYear() + "e");
        }
        double last = parseToDoubleOrZero(lastYearEps.replace(",", "."));
        double twoAgo = parseToDoubleOrZero(earningsPerShare.get(fundamentalData.getTwoYearsAgo()).replace(",", "."));
        double threeAgo = parseToDoubleOrZero(earningsPerShare.get(fundamentalData.getThreeYearsAgo()).replace(",", "."));

        double fiveYearsEarnings;
        if (next == 0 && current != 0 && last != 0 && twoAgo != 0 && threeAgo != 0) {
            fiveYearsEarnings = (current + last + twoAgo + threeAgo) / 4;
        } else {
            fiveYearsEarnings = (next + current + last + twoAgo + threeAgo) / 5;
        }

        return currentRate / fiveYearsEarnings;
    }

    /**
     * Calculate the current PER (KGV)
     *
     * @param currentRate the stocks current rate
     * @param earningsPerShare eps
     * @return the current PER
     */
    private double calculatePer(double currentRate, Map<String, String> earningsPerShare) {
        double current = parseToDoubleOrZero(earningsPerShare.get(fundamentalData.getCurrentYear()).replace(",", "."));
        return currentRate / current;
    }
}
