package jenkins.plugins.office365connector.helpers;

import hudson.model.Run;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class ClassicDisplayURLProviderBuilder extends ClassicDisplayURLProvider {

    public static final String URL = "http://localhost/job/myFirstJob/167/display/redirect";

    @Override
    public String getRunURL(Run run) {
        return URL;
    }
}
