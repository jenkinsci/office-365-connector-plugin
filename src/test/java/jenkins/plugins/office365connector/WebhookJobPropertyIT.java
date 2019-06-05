package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class WebhookJobPropertyIT {

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Test
    public void testDataCompatibility() throws Exception {
        FreeStyleProject foo = (FreeStyleProject) rule.jenkins.createProjectFromXML(
                "bar",
                getClass().getResourceAsStream("WebhookJobProperty/freestyleold1.xml")
        );
        WebhookJobProperty webhookJobProperty = foo.getProperty(WebhookJobProperty.class);
        assertThat(webhookJobProperty.getWebhooks()).isNotEmpty();
        rule.assertBuildStatusSuccess(foo.scheduleBuild2(0, new Cause.UserIdCause()).get());
    }
}
