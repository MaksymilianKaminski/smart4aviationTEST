package pl.parser.nbp;

import pl.parser.nbp.CurrencyAnalyzerData.CurrencyAnalyzerBuilder;
import pl.parser.nbp.exception.CurrencyAnalyzerException;

public class CurrencyAnalyzerFactory {
    private CurrencyAnalyzerBuilder builder;
    private CurrencyAnalyzerType analyzerType;

    public CurrencyAnalyzerFactory(CurrencyAnalyzerType analyzerType) {
        this.analyzerType = analyzerType;
    }

    public void prepareInputData(String... inputs) throws CurrencyAnalyzerException {
        if (analyzerType.equals(CurrencyAnalyzerType.NBP)) {
            if (inputs.length != 3) {
                throw new CurrencyAnalyzerException("Invalid input data, must be CURRENCY DATE_FROM DATE_TO");
            }
            builder = new CurrencyAnalyzerBuilder();
            builder.currencyCode(inputs[0]);
            builder.scopeDateFrom(inputs[1]);
            builder.scopeDateTo(inputs[2]);
        }
    }

    public CurrencyAnalyzer initCurrencyAnalyzer() throws CurrencyAnalyzerException {
        CurrencyAnalyzer currencyAnalyzer = null;

        CurrencyAnalyzerData inputData = builder.build();
        if (analyzerType.equals(CurrencyAnalyzerType.NBP)) {
            currencyAnalyzer = new NbpCurrencyAnalyzer(inputData);
        }

        if (currencyAnalyzer == null) {
            throw new CurrencyAnalyzerException("Occurred problem during initialization");
        }

        return currencyAnalyzer;
    }
}
