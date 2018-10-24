package pl.parser.nbp;

public class CurrencyAnalyzerData {
    private String scopeDateFrom;
    private String scopeDateTo;
    private String currencyCode;

    public CurrencyAnalyzerData(CurrencyAnalyzerBuilder builder) {
        this.scopeDateFrom = builder.scopeDateFrom;
        this.scopeDateTo = builder.scopeDateTo;
        this.currencyCode = builder.currencyCode;
    }

    public static class CurrencyAnalyzerBuilder {

        private String scopeDateFrom;
        private String scopeDateTo;
        private String currencyCode;

        public CurrencyAnalyzerBuilder scopeDateFrom(String scopeDateFrom) {
            this.scopeDateFrom = scopeDateFrom;
            return this;
        }

        public CurrencyAnalyzerBuilder scopeDateTo(String scopeDateTo) {
            this.scopeDateTo = scopeDateTo;
            return this;
        }

        public CurrencyAnalyzerBuilder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }


        public CurrencyAnalyzerData build() {
            return new CurrencyAnalyzerData(this);
        }
    }

    public String getScopeDateFrom() {
        return scopeDateFrom;
    }

    public String getScopeDateTo() {
        return scopeDateTo;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
