package jenkins.plugins.office365connector.workflow;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import javax.inject.Inject;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Workflow step to send a notification to Jenkins office 365 connector.
 */
public class Office365ConnectorSendStep extends AbstractStepImpl {

    private String message;
    private final String webhookUrl;
    private String status;
    private String color;

    public String getMessage() {
        return message;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        this.message = message;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public String getStatus() {
        return status;
    }

    @DataBoundSetter
    public void setStatus(String status) {
        this.status = status;
    }

    public String getColor() {
        return color;
    }

    @DataBoundSetter
    public void setColor(String color) {
        this.color = color;
    }

    @DataBoundConstructor
    public Office365ConnectorSendStep(String webhookUrl) {
        this.webhookUrl = webhookUrl;
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
            StepParameters stepParameters = new StepParameters(step.message, step.webhookUrl, step.status, step.color);
            Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, listener);
            notifier.sendBuildMessage(stepParameters);
            return null;
        }
    }
}