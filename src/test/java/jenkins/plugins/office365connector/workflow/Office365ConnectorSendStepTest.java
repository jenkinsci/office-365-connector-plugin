package jenkins.plugins.office365connector.workflow;

import hudson.util.FormValidation;
import jenkins.plugins.office365connector.helpers.ReflectionHelper;
import jenkins.plugins.office365connector.model.FactDefinition;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class Office365ConnectorSendStepTest {

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

        // when
        FormValidation result = descriptor.doCheckWebhookUrl(validUrl);

        // then
        assertThat(result, equalTo(FormValidation.ok()));
    }

    @Test
    void doCheckUrl_OnInvalidUrl_ValidatesUrl() {

        // given
        String validUrl = "-myJenkins.abc";
        Office365ConnectorSendStep.DescriptorImpl descriptor = new Office365ConnectorSendStep.DescriptorImpl();

        // when
        FormValidation result = descriptor.doCheckWebhookUrl(validUrl);

        // then
        assertThat(result.kind, equalTo(FormValidation.Kind.ERROR));
    }
}
