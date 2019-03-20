package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import jenkins.plugins.office365connector.helpers.AffectedFileBuilder;
import jenkins.plugins.office365connector.helpers.ClassicDisplayURLProviderBuilder;
import jenkins.plugins.office365connector.helpers.HttpWorkerAnswer;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import jenkins.plugins.office365connector.utils.TimeUtils;
import jenkins.plugins.office365connector.utils.TimeUtilsTest;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DisplayURLProvider.class, Office365ConnectorWebhookNotifier.class, Run.class, TimeUtils.class})
public class Office365ConnectorWebhookNotifierIntegrationTest {

    private static final String JOB_NAME = "myFirstJob";
    private static final String CAUSE_DESCRIPTION = "Started by John";
    private static final int BUILD_NUMBER = 167;
    private static final long START_TIME = 1508617305000L;
    private static final long DURATION = 1000 * 60 * 60;

    private static final String FORMATTED_START_TIME;
    private static final String FORMATTED_COMPLETED_TIME;

    static {
        TimeUtilsTest.setupTimeZoneAndLocale();
        FORMATTED_START_TIME = TimeUtils.dateToString(START_TIME);
        FORMATTED_COMPLETED_TIME = TimeUtils.dateToString(START_TIME + DURATION);
    }

    private AbstractBuild run;
    private HttpWorkerAnswer workerAnswer;

    @Before
    public void setUp() {
        mockListener();

        run = mockRun();

        mockDisplayURLProvider();
        mockEnvironment();
        mockHttpWorker();
        mockGetChangeSets();
        mockTimeUtils();
    }

    private static Job mockJob(String parentDisplayName) {
        Job job = mock(Job.class);
        ItemGroup itemGroup = mock(ItemGroup.class);
        when(itemGroup.getFullDisplayName()).thenReturn(parentDisplayName);
        when(job.getParent()).thenReturn(itemGroup);
        when(job.getFullDisplayName()).thenReturn(JOB_NAME);

        return job;
    }

    private AbstractBuild mockRun() {
        AbstractBuild run = mock(AbstractBuild.class);

        when(run.getNumber()).thenReturn(BUILD_NUMBER);
        when(run.getStartTimeInMillis()).thenReturn(START_TIME);
        when(run.getDuration()).thenReturn(DURATION);

        Job job = mockJob("");
        when(run.getParent()).thenReturn(job);

        // getProperty
        WebhookJobProperty property = new WebhookJobProperty(WebhookBuilder.sampleWebhookWithAllStatuses());
        when(job.getProperty(WebhookJobProperty.class)).thenReturn(property);

        // remarks
        Cause cause = mock(Cause.class);
        when(cause.getShortDescription()).thenReturn(CAUSE_DESCRIPTION);
        when(run.getCauses()).thenReturn(Arrays.asList(cause));

        return run;
    }

    private void mockResult(Result result) {
        when(run.getResult()).thenReturn(result);

        Run previousBuild = mock(Run.class);
        if (result == Result.FAILURE) {
            when(previousBuild.getResult()).thenReturn(Result.SUCCESS);
        }
    }

    private TaskListener mockListener() {
        TaskListener listener = mock(TaskListener.class);

        PrintStream stream = mock(PrintStream.class);
        when(listener.getLogger()).thenReturn(stream);

        return listener;
    }

    private void mockDisplayURLProvider() {
        mockStatic(DisplayURLProvider.class);
        when(DisplayURLProvider.get()).thenReturn(new ClassicDisplayURLProviderBuilder());
    }

    private void mockEnvironment() {
        EnvVars envVars = mock(EnvVars.class);
        try {
            TaskListener taskListener = Matchers.any();
            when(run.getEnvironment(taskListener)).thenReturn(envVars);
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
        when(envVars.expand(ClassicDisplayURLProviderBuilder.URL)).thenReturn(ClassicDisplayURLProviderBuilder.URL);
    }

    private void mockHttpWorker() {
        workerAnswer = new HttpWorkerAnswer();
        try {
            whenNew(HttpWorker.class).withAnyArguments().thenAnswer(workerAnswer);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void mockGetChangeSets() {
        List<ChangeLogSet> files = new AffectedFileBuilder().sampleFiles(run);
        when(run.getChangeSets()).thenReturn(files);
    }

    private void mockTimeUtils() {
        mockStatic(TimeUtils.class);
        when(TimeUtils.countCompletionTime(START_TIME, DURATION)).thenReturn(START_TIME + DURATION);
        when(TimeUtils.dateToString(START_TIME)).thenReturn(FORMATTED_START_TIME);
        when(TimeUtils.dateToString(START_TIME + DURATION)).thenReturn(FORMATTED_COMPLETED_TIME);
    }


    @Test
    public void validateStartedRequest_OnSuccess() {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertHasSameContent(workerAnswer.getData(), FileUtils.getContentFile("started-success.json"));
    }

    @Test
    public void validateCompletedRequest_OnFailure() {

        // given
        mockResult(Result.FAILURE);
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildCompletedNotification();

        // then
        assertHasSameContent(workerAnswer.getData(), FileUtils.getContentFile("completed-failure.json"));
    }

    // compares files without worrying about EOL
    private void assertHasSameContent(String value, String expected) {
        assertThat(StringUtils.normalizeSpace(value)).isEqualTo(StringUtils.normalizeSpace(expected));
    }
}
