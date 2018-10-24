package pl.parser.nbp.exception;


public class CurrencyAnalyzerException extends Exception {

    public CurrencyAnalyzerException() {
    }

    public CurrencyAnalyzerException(String message) {
        super(message);
    }

    public CurrencyAnalyzerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurrencyAnalyzerException(String message, String comment) {
        super(comment + "\n" + message);
    }

}
