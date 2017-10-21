package jenkins.plugins.office365connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import jenkins.plugins.office365connector.helpers.AffectedFileBuilder;
import jenkins.plugins.office365connector.helpers.ClassicDisplayURLProviderBuilder;
import jenkins.plugins.office365connector.helpers.HttpWorkerAnswer;
import jenkins.plugins.office365connector.helpers.WebhookBuilder;
import jenkins.plugins.office365connector.utils.TimeUtilsTest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DisplayURLProvider.class, Office365ConnectorWebhookNotifier.class, Run.class})
public class Office365ConnectorWebhookNotifierIntegrationTest {

    private static final String REQUESTS_DIRECTORY = "requests" + File.separatorChar;

    public static final String JOB_NAME = "myFirstJob";

    private static final String CAUSE_DESCRIPTION = "Started by John";
    private static final int BUILD_NUMBER = 167;
    private static final long START_TIME = 1508617305000L;

    private AbstractBuild run;
    private HttpWorkerAnswer workerAnswer;

    static {
        TimeUtilsTest.setupTimeZoneAndLocale();
    }

    @Before
    public void setUp() {
        mockListener();

        run = mockRun();

        mockDisplayURLProvider();
        mockEnvironment();
        mockHttpWorker();
        mockGetChangeSets();
    }

    private AbstractBuild mockRun() {
        AbstractBuild run = mock(AbstractBuild.class);

        when(run.getNumber()).thenReturn(BUILD_NUMBER);
        PowerMockito.when(run.getStartTimeInMillis()).thenReturn(START_TIME);

        Job job = mockJob();
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

    private Job mockJob() {
        Job job = mock(Job.class);

        when(job.getDisplayName()).thenReturn(JOB_NAME);

        return job;
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


    @Test
    public void validateRequest_OnStart() {

        // given
        Office365ConnectorWebhookNotifier notifier = new Office365ConnectorWebhookNotifier(run, mockListener());

        // when
        notifier.sendBuildStartedNotification(true);

        // then
        assertHasSameContent(workerAnswer.getData(), getContentFile("started.json"));
    }

    // compares files without worrying about EOL
    private void assertHasSameContent(String value, String expected) {
        assertThat(StringUtils.normalizeSpace(value)).isEqualTo(StringUtils.normalizeSpace(expected));
    }

    protected static String getContentFile(String fileName) {
        try {
            URL url = Office365ConnectorWebhookNotifierIntegrationTest.class.getClassLoader().getResource(REQUESTS_DIRECTORY + fileName);
            return IOUtils.toString(url.toURI(), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
