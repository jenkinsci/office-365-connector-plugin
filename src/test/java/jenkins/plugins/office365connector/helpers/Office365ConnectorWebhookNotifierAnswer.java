package jenkins.plugins.office365connector.helpers;

import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.workflow.StepParameters;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class Office365ConnectorWebhookNotifierAnswer implements Answer<Office365ConnectorWebhookNotifier> {

    private int times;

    @Override
    public Office365ConnectorWebhookNotifier answer(InvocationOnMock invocation) {
        return new Office365ConnectorWebhookNotifierMock(
                (Run) (invocation.getArguments())[0],
                (TaskListener) (invocation.getArguments())[1]);
    }

    private class Office365ConnectorWebhookNotifierMock extends Office365ConnectorWebhookNotifier {

        private Office365ConnectorWebhookNotifierMock(Run run, TaskListener listener) {
            super(run, listener);
        }

        @Override
        public void sendBuildStartedNotification(boolean isFromPreBuild) {
            times++;
        }

        @Override
        public void sendBuildCompletedNotification() {
            times++;
        }

        @Override
        public void sendBuildStepNotification(StepParameters stepParameters) {
            times++;
        }
    }

    public int getTimes() {
        return times;
    }
}