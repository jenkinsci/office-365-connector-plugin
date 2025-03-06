package jenkins.plugins.office365connector.workflow;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.helpers.ReflectionHelper;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class ExecutionTest extends AbstractTest {

    @BeforeEach
    void setUp() {
        run = mock(AbstractBuild.class);
        AbstractBuild previousBuild = mock(AbstractBuild.class);
        when(run.getPreviousBuild()).thenReturn(previousBuild);
    }

    @Test
    void onStarted_SendNotification() throws Throwable {

        // given
        StepContext stepContext = mock(StepContext.class);
        when(stepContext.get(Run.class)).thenReturn(run);
        TaskListener taskListener = mockListener();
        when(stepContext.get(TaskListener.class)).thenReturn(taskListener);
        Office365ConnectorSendStep step = new Office365ConnectorSendStep("myUrl");

        Execution execution = new Execution(step, stepContext);

        try (MockedConstruction<Office365ConnectorWebhookNotifier> notifierConstruction = mockConstruction(Office365ConnectorWebhookNotifier.class)) {
            // when
            ReflectionHelper.invokeMethod(execution,"run");

            // then
            assertEquals(1, notifierConstruction.constructed().size());
        }
    }
}
