package jenkins.plugins.office365connector.workflow;

import java.util.List;
import java.util.Set;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.model.FactDefinition;
import jenkins.plugins.office365connector.utils.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

/**
 * Workflow step to send a notification to Jenkins office 365 connector.
 */
public class Office365ConnectorSendStep extends Step {

    private final String webhookUrl;
    private String message;
    private String status;
    private List<FactDefinition> factDefinitions;
    private String color;
    private boolean adaptiveCards;
    private String credentialsId;

    @DataBoundConstructor
    public Office365ConnectorSendStep(String webhookUrl) {
        this.webhookUrl = Util.fixEmptyAndTrim(webhookUrl);
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = Util.fixEmptyAndTrim(credentialsId);
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

    public boolean isAdaptiveCards() {
        return adaptiveCards;
    }

    @DataBoundSetter
    public void setAdaptiveCards(boolean adaptiveCards) {
        this.adaptiveCards = adaptiveCards;
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
        @NonNull
        public String getDisplayName() {
            return "Send job status notifications to Office 365 (e.g. Microsoft Teams or Outlook)";
        }

        @POST
        public FormValidation doCheckWebhookUrl(@AncestorInPath Item item, @QueryParameter String value, @QueryParameter String credentialsId) {
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            } else {
                if (!item.hasPermission(Item.CONFIGURE)) {
                    return FormValidation.ok();
                }
            }
            if (StringUtils.isNotBlank(credentialsId)) {
                return FormValidation.ok();
            }
            return FormUtils.formValidateUrl(value);
        }

        @POST
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            return result
                    .includeEmptyValue()
                    .includeMatchingAs(
                            ACL.SYSTEM2,
                            item,
                            StringCredentials.class,
                            URIRequirementBuilder.fromUri("").build(),
                            CredentialsMatchers.always())
                    .includeCurrentValue(credentialsId);
        }
    }

}
