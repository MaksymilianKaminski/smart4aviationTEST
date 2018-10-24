package pl.parser.nbp;

import pl.parser.nbp.exception.CurrencyAnalyzerException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class CurrencyAnalyzer {
    protected static final DateTimeFormatter DATE_TIME_FORMATTER_LONG = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    protected static final DateTimeFormatter DATE_TIME_FORMATTER_SHORT = DateTimeFormatter.ofPattern("yyMMdd");

    protected String inputScopeDateFrom;
    protected String inputScopeDateTo;
    protected LocalDate scopeDateFrom;
    protected LocalDate scopeDateTo;
    protected String currencyCode;

    public abstract String prepareCurrencyAnalyze() throws CurrencyAnalyzerException;

    public String getInputScopeDateFrom() {
        return inputScopeDateFrom;
    }

    public void setInputScopeDateFrom(String inputScopeDateFrom) {
        this.inputScopeDateFrom = inputScopeDateFrom;
    }

    public String getInputScopeDateTo() {
        return inputScopeDateTo;
    }

    public void setInputScopeDateTo(String inputScopeDateTo) {
        this.inputScopeDateTo = inputScopeDateTo;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
