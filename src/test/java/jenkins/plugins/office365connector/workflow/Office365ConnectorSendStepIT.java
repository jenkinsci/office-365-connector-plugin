package jenkins.plugins.office365connector.workflow;

import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class Office365ConnectorSendStepIT {

    private static Office365ConnectorSendStep before = new Office365ConnectorSendStep("https://office.com");

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Test
    public void roundtrip() throws Exception {
        assertRoundTrip(before);
        before.setStatus("started");
        assertRoundTrip(before);
        before.setMessage("hello");
        assertRoundTrip(before);
        before.setColor("0000ff");
        assertRoundTrip(before);
    }

    private void assertRoundTrip(Office365ConnectorSendStep before) throws Exception {
        Office365ConnectorSendStep after = new StepConfigTester(rule).configRoundTrip(before);
        rule.assertEqualBeans(before, after, "webhookUrl");
        rule.assertEqualBeans(before, after, "status");
        rule.assertEqualBeans(before, after, "message");
        rule.assertEqualBeans(before, after, "color");
    }

}
