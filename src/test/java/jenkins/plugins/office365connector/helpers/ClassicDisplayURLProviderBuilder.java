package jenkins.plugins.office365connector.helpers;

import hudson.model.Run;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class ClassicDisplayURLProviderBuilder extends ClassicDisplayURLProvider {

    public static final String URL_TEMPLATE = "http://localhost/job/%s/%s/display/redirect";

    private int jobNumber;
    private String jobName;

    public ClassicDisplayURLProviderBuilder(String jobName, int jobNumber) {
        this.jobName = jobName;
        this.jobNumber = jobNumber;
    }

    @Override
    public String getRunURL(Run run) {
        return String.format(URL_TEMPLATE, jobName, jobNumber);
    }
}
