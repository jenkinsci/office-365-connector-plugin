package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class WebhookJobPropertyTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Test
    public void testDataCompatibility() throws Exception {
        FreeStyleProject foo = (FreeStyleProject) rule.jenkins.createProjectFromXML(
                "bar",
                getClass().getResourceAsStream("WebhookJobPropertyTest/freestyleold1.xml")
        );
        WebhookJobProperty webhookJobProperty = foo.getProperty(WebhookJobProperty.class);
        assertThat(webhookJobProperty.webhooks).isNotEmpty();
        rule.assertBuildStatusSuccess(foo.scheduleBuild2(0, new Cause.UserIdCause()).get());
    }
}
