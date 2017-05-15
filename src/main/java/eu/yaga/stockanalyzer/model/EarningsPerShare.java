package eu.yaga.stockanalyzer.model;

/**
 * Helper class that contains earnings per share for a specific point in time
 */
public class EarningsPerShare {

    private double epsCurrentYear;
    private double epsNextYear;

    /**
     * Default constructor
     */
    public EarningsPerShare() {
        epsCurrentYear = 0;
        epsNextYear = 0;
    }

    /**
     * Creats an EPS instance with the given values
     * @param epsCurrentYear this years earnings per share
     * @param epsNextYear next years earnings per share
     */
    public EarningsPerShare(double epsCurrentYear, double epsNextYear) {
        this.epsCurrentYear = epsCurrentYear;
        this.epsNextYear = epsNextYear;
    }

    public double getEpsCurrentYear() {
        return epsCurrentYear;
    }

    public void setEpsCurrentYear(double epsCurrentYear) {
        this.epsCurrentYear = epsCurrentYear;
    }

    public double getEpsNextYear() {
        return epsNextYear;
    }

    public void setEpsNextYear(double epsNextYear) {
        this.epsNextYear = epsNextYear;
    }
}
