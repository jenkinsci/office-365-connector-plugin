package jenkins.plugins.office365connector.helpers;

import hudson.model.Run;
import org.jenkinsci.plugins.displayurlapi.ClassicDisplayURLProvider;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class ClassicDisplayURLProviderBuilder extends ClassicDisplayURLProvider {

    public static final String LOCALHOST_URL_TEMPLATE = "http://localhost/job/%s/%s/display/redirect";

    private int jobNumber;
    private String jobName;
    private String generatedUrl;

    public ClassicDisplayURLProviderBuilder(String jobName, int jobNumber) {
        this.jobName = jobName;
        this.jobNumber = jobNumber;
        generatedUrl = String.format(LOCALHOST_URL_TEMPLATE, jobName, jobNumber);
    }

    public ClassicDisplayURLProviderBuilder(String jobName, int jobNumber, String urlTemplate) {
        this.jobName = jobName;
        this.jobNumber = jobNumber;
        // in case name contains parent project, drop it and leave only last part
        String pureJobName = jobName.substring(jobName.lastIndexOf(" ") + 1);
        generatedUrl = String.format(urlTemplate, pureJobName, jobNumber);
    }

    @Override
    public String getRunURL(Run run) {
        return generatedUrl;
    }
}
