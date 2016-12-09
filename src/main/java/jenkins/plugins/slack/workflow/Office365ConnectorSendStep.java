package jenkins.plugins.slack.workflow;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;

/**
 * Workflow step to send a Slack channel notification.
 */
public class Office365ConnectorSendStep extends AbstractStepImpl {

    private final @Nonnull String message;

    @Nonnull
    public String getMessage() {
        return message;
    }

    @DataBoundConstructor
    public Office365ConnectorSendStep(@Nonnull String message) {
        this.message = message;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(Office365ConnectorSendStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "office365ConnectorSend";
        }

        @Override
        public String getDisplayName() {
            return "office365ConnectorSend";
        }
    }

    public static class Office365ConnectorSendStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        transient Office365ConnectorSendStep step;

        @StepContextParameter
        transient TaskListener listener;
        
        @StepContextParameter
        transient Run run;

        @Override
        protected Void run() throws Exception {
            Office365ConnectorWebhookNotifier.sendBuildMessage(run, listener, step.message);
            return null;
        }
    }
}