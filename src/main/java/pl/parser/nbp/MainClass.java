package pl.parser.nbp;

import pl.parser.nbp.exception.CurrencyAnalyzerException;

public class MainClass {

    public static void main(String[] args) throws CurrencyAnalyzerException {
        // java -cp target/smart4aviationTEST-1.0-SNAPSHOT.jar pl.parser.nbp.MainClass EUR 2013-01-28 2013-01-31
        long start = System.currentTimeMillis();

        CurrencyAnalyzerFactory factory = new CurrencyAnalyzerFactory(CurrencyAnalyzerType.NBP);
        factory.prepareInputData(args);
        CurrencyAnalyzer currencyAnalyzer = factory.initCurrencyAnalyzer();

        String analyzeResult = currencyAnalyzer.prepareCurrencyAnalyze();
        System.out.println(analyzeResult);

        informationAboutOperationTime(start);
    }

    private static void informationAboutOperationTime(long start) {
        System.out.println("Finished - time in ms: " + (System.currentTimeMillis() - start));
    }

}