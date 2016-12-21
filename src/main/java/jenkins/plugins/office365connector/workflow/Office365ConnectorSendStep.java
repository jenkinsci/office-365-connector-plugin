package jenkins.plugins.office365connector.workflow;

import hudson.Extension;
import hudson.Util;
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
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Workflow step to send a notification to Jenkins office 365 connector.
 */
public class Office365ConnectorSendStep extends AbstractStepImpl {

    private final @Nonnull String message;
    private String webhookUrl;
    private String status;

    @Nonnull
    public String getMessage() {
        return message;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    @DataBoundSetter
    public void setWebhookUrl(String url) {
        this.webhookUrl = url;
    }

    public String getStatus() {
        return status;
    }

    @DataBoundSetter
    public void setStatus(String status) {
        this.status = status;
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
            StepParameters stepParameters = new StepParameters(step.message, step.webhookUrl, step.status);
            Office365ConnectorWebhookNotifier.sendBuildMessage(run, listener, stepParameters);
            return null;
        }
    }
}