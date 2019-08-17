package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import mockit.Deencapsulation;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Office365ConnectorWebhookNotifier.class, Execution.class})
public class ExecutionTest extends AbstractTest {

    @Before
    public void setUp() {
        run = mock(AbstractBuild.class);
        AbstractBuild previousBuild = mock(AbstractBuild.class);
        when(run.getPreviousBuild()).thenReturn(previousBuild);

        mockOffice365ConnectorWebhookNotifier();
    }

    @Test
    public void onStarted_SendNotification() throws IOException, InterruptedException {

        // given
        StepContext stepContext = mock(StepContext.class);
        when(stepContext.get(Run.class)).thenReturn(run);
        TaskListener taskListener = mockListener();
        when(stepContext.get(TaskListener.class)).thenReturn(taskListener);
        Office365ConnectorSendStep step = new Office365ConnectorSendStep("myUrl");

        Execution execution = new Execution(step, stepContext);

        // when
        Deencapsulation.invoke(execution, "run");

        // then
        assertThat(notifierAnswer.getTimes()).isOne();
    }
}
