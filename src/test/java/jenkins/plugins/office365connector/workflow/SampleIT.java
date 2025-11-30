package jenkins.plugins.office365connector.workflow;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
class SampleIT extends AbstractTest {

    private static final String JOB_NAME = "myFirst_Job_";
    private static final String CAUSE_DESCRIPTION = "Started by John";
    private static final int BUILD_NUMBER = 167;
    private static final String DEVELOPER = "Mike";

    private MockedStatic<Jenkins> staticJenkins;

    @BeforeEach
    void setUp() {
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

    @AfterEach
    void tearDown() {
        staticJenkins.close();
    }

    private AbstractBuild mockRun() {
        AbstractBuild run = mock(AbstractBuild.class);

        when(run.getNumber()).thenReturn(BUILD_NUMBER);

        Job job = mockJob(JOB_NAME);
        when(run.getParent()).thenReturn(job);

        mockProperty(job);

        return run;
    }

    private void mockGetChangeSets() {
        List<ChangeLogSet> files = new AffectedFileBuilder().singleChangeLog(run, DEVELOPER);
        when(run.getChangeSets()).thenReturn(files);
    }


    @Test
    void sendBuildStartedNotification_SendsProperData() {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertHasSameContent(workerData.get(0), FileUtils.getContentFile("started.json"));
        assertEquals(1, workerConstruction.constructed().size());
    }

    @Test
    void sendBuildStartedNotification_OnMultiplyWebhook_SendsSameData() {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());
        mockProperty(run.getParent(), WebhookBuilder.sampleMultiplyWebhookWithAllStatuses());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertThat(workerData, hasSize(2));
        assertThat(workerData.get(0), equalTo(workerData.get(1)));
        assertEquals(2, workerConstruction.constructed().size());
    }

    @Test
    void sendBuildCompletedNotification_OnSuccess_SendsProperData() {

        // given
        when(run.getResult()).thenReturn(Result.SUCCESS);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerData.get(0), FileUtils.getContentFile("completed-success.json"));
        assertEquals(1, workerConstruction.constructed().size());
    }

    @Test
    void sendBuildCompletedNotification_OnFailed_SendsProperData() {

        // given
        when(run.getResult()).thenReturn(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerData.get(0), FileUtils.getContentFile("completed-failed.json"));
        assertEquals(1, workerConstruction.constructed().size());
    }

    @Test
    void sendBuildStepNotification_SendsProperData() {

        // given
        StepParameters stepParameters = new StepParameters(
                "helloMessage", ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE,
                "funnyStatus", Collections.emptyList(), "#FF00FF", false);

        when(run.getResult()).thenReturn(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStepNotification(stepParameters);

        // then
        assertHasSameContent(workerData.get(0), FileUtils.getContentFile("sendstep.json"));
        assertEquals(1, workerConstruction.constructed().size());
    }

    @Test
    void validateCompletedRequest_OnRepeatedFailure_SendsProperData() {

        // given
        mockResult(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerData.get(0), FileUtils.getContentFile("completed-repeated_failure.json"));
        assertEquals(1, workerConstruction.constructed().size());
    }

    @Test
    void validateCompletedRequest_OnMultiplyWebhook_SendsSameData() {

        // given
        mockResult(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());
        mockProperty(run.getParent(), WebhookBuilder.sampleMultiplyWebhookWithAllStatuses());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertThat(workerData, hasSize(2));
        assertThat(workerData.get(0), equalTo(workerData.get(1)));
        assertEquals(2, workerConstruction.constructed().size());
    }

    @Test
    void sendBuildStepNotification_WithMentions_SendsProperData() {

        // given
        List<Mention> mentions = List.of(
                new Mention("test@tester.com", "test tester")
        );

        StepParameters stepParameters = new StepParameters(
                "Hi, <at>test tester</at> Build completed", ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE,
                "funnyStatus", Collections.emptyList(),"#FF00FF", true, mentions 
        );

        when(run.getResult()).thenReturn(Result.SUCCESS);
        Office365ConnectorWebhookNotifier notifier =
                new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStepNotification(stepParameters);

        // then
        assertHasSameContent(workerData.get(0),
                FileUtils.getContentFile("adaptivecard-mention.json"));
        assertEquals(1, workerConstruction.constructed().size());
    }

}
