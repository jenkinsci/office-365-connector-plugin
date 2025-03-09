package jenkins.plugins.office365connector;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class FileUtils {

    private static final String REQUESTS_DIRECTORY = "requests" + File.separatorChar;

    public static String getContentFile(String fileName) {
        try {
            URL url = FileUtils.class.getClassLoader().getResource(REQUESTS_DIRECTORY + fileName);
            return IOUtils.toString(url.toURI(), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
