package jenkins.plugins.office365connector.workflow;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import mockit.internal.reflection.MethodReflection;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class ExecutionTest extends AbstractTest {

    @Before
    public void setUp() {
        run = mock(AbstractBuild.class);
        AbstractBuild previousBuild = mock(AbstractBuild.class);
        when(run.getPreviousBuild()).thenReturn(previousBuild);
    }

    @Test
    public void onStarted_SendNotification() throws Throwable {

        // given
        StepContext stepContext = mock(StepContext.class);
        when(stepContext.get(Run.class)).thenReturn(run);
        TaskListener taskListener = mockListener();
        when(stepContext.get(TaskListener.class)).thenReturn(taskListener);
        Office365ConnectorSendStep step = new Office365ConnectorSendStep("myUrl");

        Execution execution = new Execution(step, stepContext);

        try (MockedConstruction<Office365ConnectorWebhookNotifier> notifierConstruction = mockConstruction(Office365ConnectorWebhookNotifier.class)) {
            // when
            MethodReflection.invokeWithCheckedThrows(execution.getClass(), execution, "run", new Class[]{});

            // then
            assertEquals(1, notifierConstruction.constructed().size());
        }
    }
}
