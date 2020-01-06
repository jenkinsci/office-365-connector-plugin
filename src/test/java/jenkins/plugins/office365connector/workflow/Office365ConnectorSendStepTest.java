package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import hudson.util.FormValidation;
import jenkins.plugins.office365connector.model.FactDefinition;
import mockit.Deencapsulation;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.spi.testresult.Result;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@RunWith(PowerMockRunner.class)
public class Office365ConnectorSendStepTest {

    @Test
    public void Office365ConnectorSendStep_SavesWebhook() {

        // given
        String webhook = "someString";

        // when
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(webhook);

        assertThat(step.getWebhookUrl()).isEqualTo(webhook);
    }

    @Test
    public void Office365ConnectorSendStep_TrimsWebhook() {

        // given
        String webhook = " some string ";

        // when
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(webhook);

        assertThat(step.getWebhookUrl()).isEqualTo(webhook.trim());
    }

    @Test
    public void getMessage_ReturnsMessage() {

        // given
        String message = "Hello!";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setMessage(message);

        // when
        String returnedMessage = step.getMessage();

        // then
        assertThat(returnedMessage).isEqualTo(message);
    }

    @Test
    public void getMessage_OnBlankMessage_ReturnsTrimmedMessage() {

        // given
        String message = " Hello!  ";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setMessage(message);

        // when
        String returnedMessage = step.getMessage();

        // then
        assertThat(returnedMessage).isEqualTo(message.trim());
    }

    @Test
    public void getStatus_ReturnsStatus() {

        // given
        String status = Result.FAILED.toString();
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setStatus(status);

        // when
        String returnedStatus = step.getStatus();

        // then
        assertThat(returnedStatus).isEqualTo(status);
    }

    @Test
    public void getStatus_OnBlankStatus_ReturnsTrimmedStatus() {

        // given
        String status = Result.FAILED.toString() + " ";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setStatus(status);

        // when
        String returnedStatus = step.getStatus();

        // then
        assertThat(returnedStatus).isEqualTo(status.trim());
    }

    @Test
    public void getColor_ReturnsColor() {

        // given
        String color = "#FF00BB";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setColor(color);

        // when
        String returnedColor = step.getColor();

        // then
        assertThat(returnedColor).isEqualTo(color);
    }

    @Test
    public void getFactDefinitions_ReturnsFactDefinitions() {

        // given
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        FactDefinition factDefinition = new FactDefinition("name", "theTemplate");
        step.setFactDefinitions(Arrays.asList(factDefinition));

        // when
        List<FactDefinition> returnedFactDefinitions = step.getFactDefinitions();

        // then
        assertThat(returnedFactDefinitions).containsOnly(factDefinition);
    }

    @Test
    public void getColor_OnBlankColor_ReturnsTrimmedColor() {

        // given
        String color = "black ";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setColor(color);

        // when
        String returnedColor = step.getColor();

        // then
        assertThat(returnedColor).isEqualTo(color.trim());
    }

    @Test
    public void start_CreatesExecution() {

        // given
        String message = "Hi there.";
        Office365ConnectorSendStep step = new Office365ConnectorSendStep(null);
        step.setMessage(message);
        StepContext stepContext = mock(StepContext.class);

        // when
        StepExecution execution = step.start(stepContext);

        // then
        assertThat(execution.getContext()).isEqualTo(stepContext);
        StepParameters stepParameters = Deencapsulation.getField(execution, "stepParameters");
        assertThat(stepParameters.getMessage()).isEqualTo(message);
    }

    @Test
    public void getRequiredContext_ReturnsContext() {

        // given
        StepDescriptor descriptor = new Office365ConnectorSendStep.DescriptorImpl();

        // when
        Set<? extends Class<?>> context = descriptor.getRequiredContext();

        // then
        assertThat(context).hasSize(2);
    }

    @Test
    public void getFunctionName_ReturnsFunctionName() {

        // given
        StepDescriptor descriptor = new Office365ConnectorSendStep.DescriptorImpl();

        // when
        String functionName = descriptor.getFunctionName();

        // then
        assertThat(functionName).isEqualTo("office365ConnectorSend");
    }

    @Test
    public void getDisplayName_DoesNotReturnFunctionName() {

        // given
        StepDescriptor descriptor = new Office365ConnectorSendStep.DescriptorImpl();

        // when
        String displayName = descriptor.getDisplayName();

        // then
        assertThat(displayName).isNotEqualTo("office365ConnectorSend");
    }

    @Test
    public void doCheckUrl_ValidatesUrl() {

        // given
        String validUrl = "http://myJenkins.abc";
        Office365ConnectorSendStep.DescriptorImpl descriptor = new Office365ConnectorSendStep.DescriptorImpl();

        // when
        FormValidation result = descriptor.doCheckWebhookUrl(validUrl);

        // then
        assertThat(result).isEqualTo(FormValidation.ok());
    }

    @Test
    public void doCheckUrl_OnInvalidUrl_ValidatesUrl() {

        // given
        String validUrl = "-myJenkins.abc";
        Office365ConnectorSendStep.DescriptorImpl descriptor = new Office365ConnectorSendStep.DescriptorImpl();

        // when
        FormValidation result = descriptor.doCheckWebhookUrl(validUrl);

        // then
        assertThat(result.kind).isEqualTo(FormValidation.Kind.ERROR);
    }
}
