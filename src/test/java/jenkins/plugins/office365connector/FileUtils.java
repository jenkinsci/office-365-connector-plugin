package jenkins.plugins.office365connector;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public final class FileUtils {

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
