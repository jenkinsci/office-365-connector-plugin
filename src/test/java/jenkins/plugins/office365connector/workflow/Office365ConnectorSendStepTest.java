package jenkins.plugins.office365connector.workflow;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.helpers.ReflectionHelper;
import jenkins.plugins.office365connector.model.FactDefinition;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class Office365ConnectorSendStepTest {

    private MockedStatic<Jenkins> staticJenkins;

    @AfterEach
    void tearDown() {
        if (staticJenkins != null) {
            staticJenkins.close();
        }
    }

    @Test
    void Office365ConnectorSendStep_SavesWebhook() {

        // given
        String webhook = "someString";

        // when
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(webhook);

        assertThat(step.getWebhookUrl(), equalTo(webhook));
    }

    @Test
    void Office365ConnectorSendStep_TrimsWebhook() {

        // given
        String webhook = " some string ";

        // when
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(webhook);

        assertThat(step.getWebhookUrl(), equalTo(webhook.trim()));
    }

    @Test
    void getMessage_ReturnsMessage() {

        // given
        String message = "Hello!";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setMessage(message);

        // when
        String returnedMessage = step.getMessage();

        // then
        assertThat(returnedMessage, equalTo(message));
    }

    @Test
    void getMessage_OnBlankMessage_ReturnsTrimmedMessage() {

        // given
        String message = " Hello!  ";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setMessage(message);

        // when
        String returnedMessage = step.getMessage();

        // then
        assertThat(returnedMessage, equalTo(message.trim()));
    }

    @Test
    void getStatus_ReturnsStatus() {

        // given
        String status = "FAILED";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setStatus(status);

        // when
        String returnedStatus = step.getStatus();

        // then
        assertThat(returnedStatus, equalTo(status));
    }

    @Test
    void getStatus_OnBlankStatus_ReturnsTrimmedStatus() {

        // given
        String status = "FAILED ";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setStatus(status);

        // when
        String returnedStatus = step.getStatus();

        // then
        assertThat(returnedStatus, equalTo(status.trim()));
    }

    @Test
    void getColor_ReturnsColor() {

        // given
        String color = "#FF00BB";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setColor(color);

        // when
        String returnedColor = step.getColor();

        // then
        assertThat(returnedColor, equalTo(color));
    }

    @Test
    void getFactDefinitions_ReturnsFactDefinitions() {

        // given
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        FactDefinition factDefinition = new FactDefinition("name", "theTemplate");
        step.setFactDefinitions(List.of(factDefinition));

        // when
        List<FactDefinition> returnedFactDefinitions = step.getFactDefinitions();

        // then
        assertThat(returnedFactDefinitions, contains(factDefinition));
    }

    @Test
    void getColor_OnBlankColor_ReturnsTrimmedColor() {

        // given
        String color = "black ";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setColor(color);

        // when
        String returnedColor = step.getColor();

        // then
        assertThat(returnedColor, equalTo(color.trim()));
    }

    @Test
    void start_CreatesExecution() {

        // given
        String message = "Hi there.";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setMessage(message);
        StepContext stepContext = mock(StepContext.class);

        // when
        StepExecution execution = step.start(stepContext);

        // then
        assertThat(execution.getContext(), equalTo(stepContext));
        StepParameters stepParameters = ReflectionHelper.getField(execution, "stepParameters");
        assertThat(stepParameters.getMessage(), equalTo(message));
    }

    @Test
    void getRequiredContext_ReturnsContext() {

        // given
        StepDescriptor descriptor = new Office365ConnectorSendStep.DescriptorImpl();

        // when
        Set<? extends Class<?>> context = descriptor.getRequiredContext();

        // then
        assertThat(context, hasSize(2));
    }

    @Test
    void getFunctionName_ReturnsFunctionName() {

        // given
        StepDescriptor descriptor = new Office365ConnectorSendStep.DescriptorImpl();

        // when
        String functionName = descriptor.getFunctionName();

        // then
        assertThat(functionName, equalTo("office365ConnectorSend"));
    }

    @Test
    void getDisplayName_DoesNotReturnFunctionName() {

        // given
        StepDescriptor descriptor = new Office365ConnectorSendStep.DescriptorImpl();

        // when
        String displayName = descriptor.getDisplayName();
        String functionName = descriptor.getFunctionName();

        // then
        assertThat(displayName, not(blankString()));
        assertThat(displayName, not(equalTo(functionName)));
    }

    @Test
    void doCheckUrl_ValidatesUrl() {

        // given
        String validUrl = "http://myJenkins.abc";
        Office365ConnectorSendStep.DescriptorImpl descriptor = new Office365ConnectorSendStep.DescriptorImpl();
        Item item = mock(Item.class);
        when(item.hasPermission(Item.CONFIGURE)).thenReturn(true);

        // when
        FormValidation result = descriptor.doCheckWebhookUrl(item, validUrl, "");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckUrl_OnInvalidUrl_ValidatesUrl() {

        // given
        String validUrl = "-myJenkins.abc";
        Office365ConnectorSendStep.DescriptorImpl descriptor = new Office365ConnectorSendStep.DescriptorImpl();
        Item item = mock(Item.class);
        when(item.hasPermission(Item.CONFIGURE)).thenReturn(true);

        // when
        FormValidation result = descriptor.doCheckWebhookUrl(item, validUrl, "");

        // then
        assertThat(result.kind, equalTo(FormValidation.Kind.ERROR));
    }

    @Test
    void getCredentialsId_ReturnsCredentialsId() {

        // given
        String credId = "my-secret-id";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setCredentialsId(credId);

        // when
        String returnedCredentialsId = step.getCredentialsId();

        // then
        assertThat(returnedCredentialsId, equalTo(credId));
    }

    @Test
    void getCredentialsId_OnBlankCredentialsId_ReturnsNull() {

        // given
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setCredentialsId("  ");

        // when
        String returnedCredentialsId = step.getCredentialsId();

        // then
        assertThat(returnedCredentialsId, nullValue());
    }

    @Test
    void doCheckWebhookUrl_WithCredentialsId_ReturnsOk() {

        // given
        Office365ConnectorSendStep.DescriptorImpl descriptor = new Office365ConnectorSendStep.DescriptorImpl();
        Item item = mock(Item.class);
        when(item.hasPermission(Item.CONFIGURE)).thenReturn(true);

        // when
        FormValidation result = descriptor.doCheckWebhookUrl(item, "", "my-credential");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckWebhookUrl_WithNoPermission_ReturnsOk() {

        // given
        Office365ConnectorSendStep.DescriptorImpl descriptor = new Office365ConnectorSendStep.DescriptorImpl();
        Item item = mock(Item.class);
        when(item.hasPermission(Item.CONFIGURE)).thenReturn(false);

        // when
        FormValidation result = descriptor.doCheckWebhookUrl(item, "invalid", "");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckWebhookUrl_WithNullItem_AndNoPermission_ReturnsOk() {

        // given
        Office365ConnectorSendStep.DescriptorImpl descriptor = new Office365ConnectorSendStep.DescriptorImpl();
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);
        when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(false);

        // when
        FormValidation result = descriptor.doCheckWebhookUrl(null, "invalid", "");

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doFillCredentialsIdItems_WithNoPermission_ReturnsCurrentValue() {

        // given
        Office365ConnectorSendStep.DescriptorImpl descriptor = new Office365ConnectorSendStep.DescriptorImpl();
        Item item = mock(Item.class);
        when(item.hasPermission(Item.EXTENDED_READ)).thenReturn(false);
        when(item.hasPermission(CredentialsProvider.USE_ITEM)).thenReturn(false);

        // when
        ListBoxModel result = descriptor.doFillCredentialsIdItems(item, "existing-id");

        // then
        assertThat(result.stream().anyMatch(o -> "existing-id".equals(o.value)), equalTo(true));
    }

    @Test
    void doFillCredentialsIdItems_WithNullItem_AndNoPermission_ReturnsCurrentValue() {

        // given
        Office365ConnectorSendStep.DescriptorImpl descriptor = new Office365ConnectorSendStep.DescriptorImpl();
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        staticJenkins.when(Jenkins::get).thenReturn(jenkins);
        when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(false);

        // when
        ListBoxModel result = descriptor.doFillCredentialsIdItems(null, "existing-id");

        // then
        assertThat(result.stream().anyMatch(o -> "existing-id".equals(o.value)), equalTo(true));
    }
}
