package pl.parser.nbp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pl.parser.nbp.exception.CurrencyAnalyzerException;
import pl.parser.nbp.types.FileFormat;
import pl.parser.nbp.utils.FileUtils;
import pl.parser.nbp.utils.MathUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

public class NbpCurrencyAnalyzer extends CurrencyAnalyzer {
    private static final Logger logger = Logger.getLogger(NbpCurrencyAnalyzer.class.toString());
    private static final String RESOURCE_DESTINATION_PATH = "src/main/resources/nbpfiles/";
    private static final String NBP_RESOURCE_DESTINATION_URL_PATH = "http://www.nbp.pl/kursy/xml/";
    private static final String SIGN_MARKED_CURRENCY_FILE = "c";
    private static final String GUIDE_NBP_CURRENCY_FILENAME = "dir";
    private static final String FIRST_WORK_DAY_OF_YEAR = "0102";
    private static final String LAST_WORK_DAY_OF_YEAR = "1231";
    private static final int LATEST_YEAR_FOR_NBP_CURRENCY_FILE = 2002;

    private List<String> scopeFilePaths = new LinkedList<>();

    public NbpCurrencyAnalyzer(CurrencyAnalyzerData inputData) {
        this.inputScopeDateFrom = inputData.getScopeDateFrom();
        this.inputScopeDateTo = inputData.getScopeDateTo();
        this.currencyCode = inputData.getCurrencyCode();
    }

    @Override
    public String prepareCurrencyAnalyze() throws CurrencyAnalyzerException {
        StringBuilder analyzeResult = new StringBuilder("\n");

        parseInputDate();
        checkDateOrdered();

        int scopeYearFrom = scopeDateFrom.getYear();
        int scopeYearTo = scopeDateTo.getYear();
        List<Integer> yearScopes = prepareYearScopes(scopeYearFrom, scopeYearTo);

        prepareScopeFilePaths(yearScopes, scopeYearFrom, scopeYearTo);
        downloadScopeFilePaths(scopeFilePaths);

        if (!scopeFilePaths.isEmpty()) {
            List<Double> buyingCurrencyRates = new ArrayList<>();
            List<Double> sellingCurrencyRates = new ArrayList<>();
            parseCurrencyRates(scopeFilePaths, buyingCurrencyRates, sellingCurrencyRates);

            if (buyingCurrencyRates.isEmpty() || sellingCurrencyRates.isEmpty()) {
                throw new CurrencyAnalyzerException("Invalid currency code or files currency content");
            }

            double avgBuyingCurrency = calculateAvgBuyingCurrency(buyingCurrencyRates);
            double standardDeviationSellingCurrency = calculateStandardDeviationSellingCurrency(sellingCurrencyRates);

            return analyzeResult.append(currencyCode).append(" › kod waluty").append("\n")
                    .append(inputScopeDateFrom).append(" › data początkowa").append("\n")
                    .append(inputScopeDateTo).append(" › data końcowa").append("\n")
                    .append(avgBuyingCurrency).append(" › średni kurs kupna").append("\n")
                    .append(standardDeviationSellingCurrency).append(" › odchylenie standardowe kursów sprzedaży").append("\n")
                    .toString();
        }

        return analyzeResult.toString();
    }

    private void parseInputDate() throws CurrencyAnalyzerException {
        LocalDate currentDay = LocalDate.now();
        try {
            scopeDateFrom = LocalDate.parse(inputScopeDateFrom, DATE_TIME_FORMATTER_LONG);
            scopeDateTo = LocalDate.parse(inputScopeDateTo, DATE_TIME_FORMATTER_LONG);
        } catch (DateTimeException dte) {
            throw new CurrencyAnalyzerException(dte.getMessage(), "Invalid input date - must be yyyy-mm-dd");
        }

        if (scopeDateFrom.isAfter(currentDay) || scopeDateTo.isAfter(currentDay)) {
            throw new CurrencyAnalyzerException("Invalid input date - date after current day");
        }

        if (scopeDateFrom.getYear() < LATEST_YEAR_FOR_NBP_CURRENCY_FILE || scopeDateTo.getYear() < LATEST_YEAR_FOR_NBP_CURRENCY_FILE) {
            throw new CurrencyAnalyzerException("Invalid input date - no information about this part of period");
        }
    }

    private void checkDateOrdered() {
        if (scopeDateFrom.isAfter(scopeDateTo)) {
            LocalDate tmp = scopeDateFrom;
            scopeDateFrom = scopeDateTo;
            scopeDateTo = tmp;

            String tmpInput = inputScopeDateFrom;
            inputScopeDateFrom = inputScopeDateTo;
            inputScopeDateTo = tmpInput;
            logger.warning("Dates had been ordered");
        }
    }

    private LocalDate parseShortDateFormat(String inputDate) throws CurrencyAnalyzerException {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(inputDate, DATE_TIME_FORMATTER_SHORT);
        } catch (DateTimeException dte) {
            throw new CurrencyAnalyzerException(dte.getMessage(), "Occurred problem with date parsing");
        }
        return parsedDate;
    }

    private List<Integer> prepareYearScopes(int scopeYearFrom, int scopeYearTo) {
        List<Integer> yearScopes = new ArrayList<>();
        for (int i = scopeYearFrom; i <= scopeYearTo; i++) {
            yearScopes.add(i);
        }
        if (scopeYearFrom > scopeYearTo) {
            Collections.reverse(yearScopes);
        }

        return yearScopes;
    }

    private void prepareScopeFilePaths(List<Integer> yearScopes, int scopeYearFrom, int scopeYearTo) throws CurrencyAnalyzerException {
        for (Integer year : yearScopes) {
            String substringYear = year.toString().substring(2, 4);

            String fileNameWithYearContent = prepareFileNameWithYearContent(year);
            String destinationURLPath = NBP_RESOURCE_DESTINATION_URL_PATH + fileNameWithYearContent;
            String destinationResourcePath = RESOURCE_DESTINATION_PATH + fileNameWithYearContent;
            FileUtils.checkFileExistOrDownload(destinationResourcePath, destinationURLPath);

            LocalDate scopeFromBySelectedYear = parseShortDateFormat(substringYear + FIRST_WORK_DAY_OF_YEAR);
            LocalDate scopeToBySelectedYear = parseShortDateFormat(substringYear + LAST_WORK_DAY_OF_YEAR);
            if (year.equals(scopeYearFrom) && year.equals(scopeYearTo)) {
                scopeFromBySelectedYear = scopeDateFrom;
                scopeToBySelectedYear = scopeDateTo;
            } else if (year.equals(scopeYearFrom) && !year.equals(scopeYearTo)) {
                scopeFromBySelectedYear = scopeDateFrom;
            } else if (!year.equals(scopeYearFrom) && year.equals(scopeYearTo)) {
                scopeToBySelectedYear = scopeDateTo;
            }


            writeToScopeFilePaths(destinationResourcePath, substringYear, scopeFromBySelectedYear, scopeToBySelectedYear);
        }
    }

    private void writeToScopeFilePaths(String destinationResourcePath, String substringYear, LocalDate scopeFromBySelectedYear, LocalDate scopeToBySelectedYear) throws CurrencyAnalyzerException {
        try {
            Scanner scanner = new Scanner(new File(destinationResourcePath));

            boolean loadLineWithRange = false;
            while (scanner.hasNextLine()) {
                String lineForCurrentYear = scanner.nextLine();

                if (!lineForCurrentYear.startsWith(SIGN_MARKED_CURRENCY_FILE))
                    continue;

                String monthNo = lineForCurrentYear.substring(7, 9);
                String dayNo = lineForCurrentYear.substring(9, 11);
                LocalDate lineDate = parseShortDateFormat(substringYear + monthNo + dayNo);

                if (!loadLineWithRange && (lineDate.isEqual(scopeFromBySelectedYear) || lineDate.isAfter(scopeFromBySelectedYear))) {
                    loadLineWithRange = true;
                }

                if (loadLineWithRange) {
                    scopeFilePaths.add(lineForCurrentYear);

                    if (lineDate.isEqual(scopeToBySelectedYear)) {
                        break;
                    } else if (lineDate.isAfter(scopeToBySelectedYear)) {
                        scopeFilePaths.remove(scopeFilePaths.size() - 1);
                        break;
                    }
                }
            }

        } catch (FileNotFoundException e) {
            throw new CurrencyAnalyzerException(e.getMessage(), "Occurred problem with getting files from server");
        }
    }

    private void downloadScopeFilePaths(List<String> scopeFilePaths) throws CurrencyAnalyzerException {
        if (!scopeFilePaths.isEmpty()) {
            logger.info("Downloading files in process ...");
            for (String filePath : scopeFilePaths) {
                String fileName = filePath + FileFormat.XML.getValue();
                String destinationResourcePath = RESOURCE_DESTINATION_PATH + fileName;
                String destinationURLPath = NBP_RESOURCE_DESTINATION_URL_PATH + fileName;

                FileUtils.checkFileExistOrDownload(destinationResourcePath, destinationURLPath);
            }
            logger.info("Files have been downloaded");
        } else {
            System.out.println("\nNie znaleziono informacji o walutach w podanym zakresie");
        }
    }

    private String prepareFileNameWithYearContent(Integer year) {
        if (year.equals(LocalDate.now().getYear())) {
            return GUIDE_NBP_CURRENCY_FILENAME + FileFormat.TXT.getValue();
        } else {
            return GUIDE_NBP_CURRENCY_FILENAME + year + FileFormat.TXT.getValue();
        }
    }

    private void parseCurrencyRates(List<String> scopeFilePaths, List<Double> buyingCurrencyRates, List<Double> sellingCurrencyRates) throws CurrencyAnalyzerException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            for (String filePath : scopeFilePaths) {
                String fileName = filePath + FileFormat.XML.getValue();
                String destinationResourcePath = RESOURCE_DESTINATION_PATH + fileName;
                File inputFile = new File(destinationResourcePath);

                Document doc = dBuilder.parse(inputFile);

                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName("pozycja");

                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String elementCurrencyCode = eElement
                                .getElementsByTagName("kod_waluty")
                                .item(0)
                                .getTextContent();
                        if (currencyCode.equals(elementCurrencyCode)) {
                            NumberFormat format = NumberFormat.getInstance(Locale.FRENCH);
                            Double buyCurrencyRate = format.parse(eElement
                                    .getElementsByTagName("kurs_kupna")
                                    .item(0)
                                    .getTextContent()).doubleValue();
                            buyingCurrencyRates.add(buyCurrencyRate);

                            Double sellingCurrencyRate = format.parse(eElement
                                    .getElementsByTagName("kurs_sprzedazy")
                                    .item(0)
                                    .getTextContent()).doubleValue();
                            sellingCurrencyRates.add(sellingCurrencyRate);

                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new CurrencyAnalyzerException(e.getMessage(), "Occurred problem with files parsing");
        }
    }

    private double calculateAvgBuyingCurrency(List<Double> buyingCurrencyRates) {
        double sumForBuyingCurrency = buyingCurrencyRates.stream().mapToDouble(Double::doubleValue).sum();
        double avgForBuyingCurrency = sumForBuyingCurrency / (double) buyingCurrencyRates.size();
        return MathUtils.round(avgForBuyingCurrency);
    }

    private double calculateStandardDeviationSellingCurrency(List<Double> sellingCurrencyRates) {
        double sumForSellingCurrency = sellingCurrencyRates.stream().mapToDouble(Double::doubleValue).sum();
        double avgForSellingCurrency = sumForSellingCurrency / (double) sellingCurrencyRates.size();

        double result = 0;
        for (Double sellingCurrency : sellingCurrencyRates) {
            result = result + Math.pow(sellingCurrency - avgForSellingCurrency, 2) / sellingCurrencyRates.size();
        }
        return MathUtils.round(Math.sqrt(result));
    }

}

