package jenkins.plugins.office365connector.workflow;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import javax.annotation.Nonnull;
import jenkins.plugins.office365connector.model.FactDefinition;
import jenkins.plugins.office365connector.utils.FormUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * Workflow step to send a notification to Jenkins office 365 connector.
 */
public class Office365ConnectorSendStep extends Step {

    private final String webhookUrl;
    private String message;
    private String status;
    private List<FactDefinition> factDefinitions;
    private String color;

    @DataBoundConstructor
    public Office365ConnectorSendStep(String webhookUrl) {
        this.webhookUrl = Util.fixEmptyAndTrim(webhookUrl);
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public String getMessage() {
        return message;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        this.message = Util.fixEmptyAndTrim(message);
    }

    public String getStatus() {
        return status;
    }

    // TODO: This is not validated anyway so this may be like "crazyyyyStatu$"
    @DataBoundSetter
    public void setStatus(String status) {
        this.status = Util.fixEmptyAndTrim(status);
    }

    public String getColor() {
        return color;
    }

    @DataBoundSetter
    public void setFactDefinitions(List<FactDefinition> factDefinitions) {
        this.factDefinitions = factDefinitions;
    }

    public List<FactDefinition> getFactDefinitions() {
        return factDefinitions;
    }

    @DataBoundSetter
    public void setColor(String color) {
        this.color = Util.fixEmptyAndTrim(color);
    }

    @Override
    public StepExecution start(StepContext context) {
        return new Execution(this, context);
    }

    @Extension
    @Symbol("office365ConnectorSend")
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "office365ConnectorSend";
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return "Send job status notifications to Office 365 (e.g. Microsoft Teams or Outlook)";
        }

        public FormValidation doCheckWebhookUrl(@QueryParameter String value) {
            return FormUtils.formValidateUrl(value);
        }
    }

}
