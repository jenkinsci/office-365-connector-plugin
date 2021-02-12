package jenkins.plugins.office365connector.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Office365ConnectorWebhookNotifier.class, Jenkins.class})
public class SampleIT extends AbstractTest {

    private static final String JOB_NAME = "myFirst_Job_";
    private static final String CAUSE_DESCRIPTION = "Started by John";
    private static final int BUILD_NUMBER = 167;
    private static final String DEVELOPER = "Mike";

    @Before
    public void setUp() {
        mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        mockListener();

        run = mockRun();
        mockCause(CAUSE_DESCRIPTION);

        mockDisplayURLProvider(JOB_NAME, BUILD_NUMBER);
        mockEnvironment();
        mockHttpWorker();
        mockGetChangeSets();

        when(Jenkins.get()).thenReturn(jenkins);

        Webhook.DescriptorImpl mockDescriptor = mock(Webhook.DescriptorImpl.class);
        when(mockDescriptor.getName()).thenReturn("testName");

        when(jenkins.getDescriptorOrDie(Webhook.class)).thenReturn(mockDescriptor);
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
    public void sendBuildStartedNotification_SendsProperData() {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertHasSameContent(workerAnswer.getData(), FileUtils.getContentFile("started.json"));
        assertThat(workerAnswer.getTimes()).isOne();
    }

    @Test
    public void sendBuildStartedNotification_OnMultiplyWebhook_SendsSameData() {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());
        mockProperty(run.getParent(), WebhookBuilder.sampleMultiplyWebhookWithAllStatuses());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertThat(workerAnswer.getAllData()).hasSize(2);
        assertThat(workerAnswer.getAllData().get(0)).isEqualTo(workerAnswer.getAllData().get(1));
        assertThat(workerAnswer.getTimes()).isEqualTo(2);
    }

    @Test
    public void sendBuildCompletedNotification_OnSuccess_SendsProperData() {

        // given
        when(run.getResult()).thenReturn(Result.SUCCESS);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerAnswer.getData(), FileUtils.getContentFile("completed-success.json"));
        assertThat(workerAnswer.getTimes()).isOne();
    }

    @Test
    public void sendBuildCompletedNotification_OnFailed_SendsProperData() {

        // given
        when(run.getResult()).thenReturn(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerAnswer.getData(), FileUtils.getContentFile("completed-failed.json"));
        assertThat(workerAnswer.getTimes()).isOne();
    }

    @Test
    public void sendBuildStepNotification_SendsProperData() {

        // given
        StepParameters stepParameters = new StepParameters(
                "helloMessage", ClassicDisplayURLProviderBuilder.LOCALHOST_URL_TEMPLATE,
                "funnyStatus", Collections.emptyList(), "#FF00FF");

        when(run.getResult()).thenReturn(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStepNotification(stepParameters);

        // then
        assertHasSameContent(workerAnswer.getData(), FileUtils.getContentFile("sendstep.json"));
        assertThat(workerAnswer.getTimes()).isOne();
    }

    @Test
    public void validateCompletedRequest_OnRepeatedFailure_SendsProperData() {

        // given
        mockResult(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerAnswer.getData(), FileUtils.getContentFile("completed-repeated_failure.json"));
        assertThat(workerAnswer.getTimes()).isOne();
    }

    @Test
    public void validateCompletedRequest_OnMultiplyWebhook_SendsSameData() {

        // given
        mockResult(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());
        mockProperty(run.getParent(), WebhookBuilder.sampleMultiplyWebhookWithAllStatuses());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertThat(workerAnswer.getAllData()).hasSize(2);
        assertThat(workerAnswer.getAllData().get(0)).isEqualTo(workerAnswer.getAllData().get(1));
        assertThat(workerAnswer.getTimes()).isEqualTo(2);
    }
}
