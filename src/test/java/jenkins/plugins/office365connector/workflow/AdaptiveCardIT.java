package jenkins.plugins.office365connector.workflow;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Result;
import hudson.scm.ChangeLogSet;
import jenkins.model.Jenkins;
import jenkins.plugins.office365connector.FileUtils;
import jenkins.plugins.office365connector.Office365ConnectorWebhookNotifier;
import jenkins.plugins.office365connector.Webhook;
import jenkins.plugins.office365connector.helpers.AffectedFileBuilder;
import jenkins.plugins.office365connector.helpers.ClassicDisplayURLProviderBuilder;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

/**
 * @author Markus Helbig (markush81@github)
 */
public class AdaptiveCardIT extends AbstractTest {

    private static final String JOB_NAME = "myFirst_Job_";
    private static final String CAUSE_DESCRIPTION = "Started by John";
    private static final int BUILD_NUMBER = 167;
    private static final String DEVELOPER = "Mike";

    private MockedStatic<Jenkins> staticJenkins;

    @Before
    public void setUp() {
        staticJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        mockListener();

        run = mockRun();
        mockCause(CAUSE_DESCRIPTION);

        mockDisplayURLProvider(JOB_NAME, BUILD_NUMBER);
        mockEnvironment();
        mockHttpWorker();
        mockGetChangeSets();

        staticJenkins.when(Jenkins::get).thenReturn(jenkins);

        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(mockDescriptor.getName()).thenReturn("testName");

        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
    }

    @After
    public void tearDown() {
        staticJenkins.close();
    }

    private AbstractBuild mockRun() {
        AbstractBuild run = mock(AbstractBuild.class);

        when(run.getNumber()).thenReturn(BUILD_NUMBER);

        Job job = mockJob(JOB_NAME);
        when(run.getParent()).thenReturn(job);

        mockProperty(job, WebhookBuilder.sampleWebhookWithAllStatusesAdaptiveCard());

        return run;
    }

    private void mockGetChangeSets() {
        List<ChangeLogSet> files = new AffectedFileBuilder().singleChangeLog(run, DEVELOPER);
        when(run.getChangeSets()).thenReturn(files);
    }


    @Test
    public void testAdaptiveCardStarted() {

        // given
        when(run.getResult()).thenReturn(Result.SUCCESS);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerData.get(0), FileUtils.getContentFile("adaptivecard-success.json"));
        assertEquals(1, workerConstruction.constructed().size());
    }

    @Test
    public void testAdaptiveCardStep() {

        // given
        StepParameters stepParameters = new StepParameters(
                "helloMessage", ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE,
                "funnyStatus", Collections.emptyList(), "#FF00FF", false);

        when(run.getResult()).thenReturn(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStepNotification(stepParameters);

        // then
        assertHasSameContent(workerData.get(0), FileUtils.getContentFile("adaptivecard-step.json"));
        assertEquals(1, workerConstruction.constructed().size());
    }
}
