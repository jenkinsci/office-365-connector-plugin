package jenkins.plugins.office365connector.workflow;

import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class Office365ConnectorSendStepTest {

    private static Office365ConnectorSendStep before = new Office365ConnectorSendStep("https://office.com");
    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Test
    public void roundtrip() throws Exception {
        configRoundTrip(before);
        before.setStatus("started");
        configRoundTrip(before);
        before.setMessage("hello");
        configRoundTrip(before);
        before.setColor("0000ff");
        configRoundTrip(before);
    }

    private void configRoundTrip(Office365ConnectorSendStep before) throws Exception {
        Office365ConnectorSendStep after = new StepConfigTester(rule).configRoundTrip(before);
        rule.assertEqualBeans(before, after, "webhookUrl");
        rule.assertEqualBeans(before, after, "status");
        rule.assertEqualBeans(before, after, "message");
        rule.assertEqualBeans(before, after, "color");
    }

}
