package pl.parser.nbp.utils;

import pl.parser.nbp.exception.CurrencyAnalyzerException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtils {

    public static void checkFileExistOrDownload(String destinationResourcePath, String destinationURLPath) throws CurrencyAnalyzerException {
        if (!Files.exists(Paths.get(destinationResourcePath))) {
            try {
                download(destinationResourcePath, destinationURLPath);
            } catch (IOException e) {
                throw new CurrencyAnalyzerException(e.getMessage(), "Occurred problem with downloading files from NBP website. ");
            }
        }
    }

    private static Path download(String resourcePath, String sourceURL) throws IOException {
        URL url = new URL(sourceURL);
        Path targetPath = new File(resourcePath).toPath();
        Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath;
    }
}
