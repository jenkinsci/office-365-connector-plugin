package jenkins.plugins.office365connector;

import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

@WithJenkins
class WebhookJobPropertyIT {

    @Test
    void testDataCompatibility(JenkinsRule rule) throws Exception {

        // given
        FreeStyleProject foo = (FreeStyleProject) rule.jenkins.createProjectFromXML(
                "bar",
                getClass().getResourceAsStream("WebhookJobProperty/freestyleold1.xml")
        );

        // when
        WebhookJobProperty webhookJobProperty = foo.getProperty(WebhookJobProperty.class);
        assertThat(webhookJobProperty.getWebhooks(), not(empty()));

        // then
        rule.assertBuildStatusSuccess(foo.scheduleBuild2(0, new Cause.UserIdCause()).get());
    }
}
