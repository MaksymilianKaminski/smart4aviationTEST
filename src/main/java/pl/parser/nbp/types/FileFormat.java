package pl.parser.nbp.types;

public enum FileFormat {
    TXT(".txt"),XML(".xml");

    private String value;

    FileFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
